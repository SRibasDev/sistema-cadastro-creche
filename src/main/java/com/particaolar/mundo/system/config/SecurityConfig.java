package com.particaolar.mundo.system.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.particaolar.mundo.system.exception.handler.ErrorResponse;
import com.particaolar.mundo.system.security.filter.JwtAuthFilter;
import com.particaolar.mundo.system.security.service.UsuarioService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UsuarioService usuarioService;
    private final ObjectMapper objectMapper;

    /**
     * Origens CORS permitidas, separadas por vírgula (padrões, ex.:
     * https://app.exemplo.com.br). SaaS: ponto único de configuração de origem.
     */
    @Value("${cors.allowed-origins:http://localhost:[*],https://localhost:[*]}")
    private String allowedOrigins;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(headers -> headers
                        .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; frame-ancestors 'none';"))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                                        "Não autorizado",
                                        "Você precisa se autenticar para acessar este recurso"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                                        "Acesso negado",
                                        "Você não tem permissão para acessar este recurso"))
                )
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/login").permitAll()

                        // Auth management — ADMIN only. /register always creates
                        // FUNCIONARIO; /usuarios pode criar qualquer perfil.
                        .requestMatchers(HttpMethod.POST, "/api/auth/register").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/auth/usuarios").hasRole("ADMIN")

                        // Static UI & assets (test interface)
                        .requestMatchers("/", "/index.html", "/favicon.ico").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()

                        // Swagger/OpenAPI — ADMIN only (disable via SWAGGER_ENABLED=false in prod)
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html",
                                "/v3/api-docs/**").hasRole("ADMIN")

                        // Actuator - health public, rest for ADMIN
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")

                        // Presence: FUNCIONARIO may mark presence only
                        .requestMatchers(HttpMethod.POST, "/api/presencas/**").hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.PATCH, "/api/presencas/**").hasAnyRole("ADMIN", "FUNCIONARIO")

                        // All other writes are ADMIN only (reservas, tutores, pets,
                        // servicos, pacotes, cobrancas...)
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                        // Reads for both profiles
                        .requestMatchers(HttpMethod.GET, "/api/**").hasAnyRole("ADMIN", "FUNCIONARIO")

                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(s -> !s.isEmpty())
                        .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With"
        ));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(usuarioService);
        provider.setPasswordEncoder(passwordEncoder());
        provider.setHideUserNotFoundExceptions(true);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    private void writeError(HttpServletResponse response, int status, String erro, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(),
                new ErrorResponse(status, erro, mensagem, LocalDateTime.now()));
    }
}