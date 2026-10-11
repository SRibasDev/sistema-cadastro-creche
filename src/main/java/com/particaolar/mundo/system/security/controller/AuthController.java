package com.particaolar.mundo.system.security.controller;

import com.particaolar.mundo.system.security.dto.LoginRequestDTO;
import com.particaolar.mundo.system.security.dto.LoginResponseDTO;
import com.particaolar.mundo.system.security.dto.RegisterRequestDTO;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.repository.UsuarioRepository;
import com.particaolar.mundo.system.security.service.JwtService;
import com.particaolar.mundo.system.security.service.UsuarioService;
import com.particaolar.mundo.system.support.NormalizadorDados;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO dto) {
        String email = NormalizadorDados.normalizarEmail(dto.email());
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, dto.senha())
            );
        } catch (AuthenticationException e) {
            // Mensagem genérica para evitar enumeração de usuários.
            throw new BadCredentialsException("Credenciais inválidas");
        }

        Usuario usuario = (Usuario) usuarioService.loadUserByUsername(email);
        String token = jwtService.gerarToken(usuario);
        // Log sem dados sensíveis: apenas o identificador interno.
        log.info("Login bem-sucedido para usuário id={}", usuario.getId());
        return ResponseEntity.ok(new LoginResponseDTO(token, usuario.getNome(), usuario.getRole().name()));
    }

    /**
     * Criação de usuários (dono/funcionários). EXCLUSIVA PARA ADMIN.
     *
     * <p>Proteção em profundidade: além do matcher de rota exigir ROLE_ADMIN,
     * o método exige {@code @PreAuthorize}. Nunca confia apenas no frontend.</p>
     *
     * <p>O perfil ({@code role}) é definido por um ADMIN autenticado. Como só um ADMIN
     * chega até aqui, não há auto-elevação por registro anônimo.</p>
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequestDTO dto) {
        String email = NormalizadorDados.normalizarEmail(dto.email());
        if (usuarioRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(dto.senha()));
        usuario.setRole(dto.role());
        usuarioRepository.save(usuario);
        log.info("Novo usuário registrado id={} role={}", usuario.getId(), usuario.getRole());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}