package com.particaolar.mundo.system.security;

import com.particaolar.mundo.system.enums.Role;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.repository.UsuarioRepository;
import com.particaolar.mundo.system.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Testes de regressão das vulnerabilidades críticas de autenticação/autorização.
 *
 * <p>Cobre:</p>
 * <ul>
 *   <li>Registro anônimo NÃO pode criar contas (era o vetor de auto-elevação a ADMIN).</li>
 *   <li>FUNCIONARIO NÃO pode criar usuários.</li>
 *   <li>ADMIN pode criar usuários.</li>
 *   <li>Swagger/API-docs NÃO ficam públicos sem autenticação.</li>
 * </ul>
 */
@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:secreg;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "jwt.secret=regression_secret_key_for_tests_only_min_32_chars",
        "jwt.expiration=3600000",
        "springdoc.swagger-ui.enabled=false",
        "springdoc.api-docs.enabled=false"
})
@AutoConfigureMockMvc
class AuthSecurityRegressionTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String adminToken;
    private String funcionarioToken;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        Usuario admin = new Usuario();
        admin.setNome("Admin"); admin.setEmail("admin@reg.com");
        admin.setSenha(passwordEncoder.encode("Admin@12345"));
        admin.setRole(Role.ADMIN);
        usuarioRepository.save(admin);
        adminToken = "Bearer " + jwtService.gerarToken(admin);

        Usuario func = new Usuario();
        func.setNome("Func"); func.setEmail("func@reg.com");
        func.setSenha(passwordEncoder.encode("Func@12345"));
        func.setRole(Role.FUNCIONARIO);
        usuarioRepository.save(func);
        funcionarioToken = "Bearer " + jwtService.gerarToken(func);
    }

    private String registerJson(String nome, String email, String senha, String role) {
        return """
                {
                  "nome": "%s",
                  "email": "%s",
                  "senha": "%s",
                  "role": "%s"
                }
                """.formatted(nome, email, senha, role);
    }

    @Test
    @DisplayName("REGRESSÃO: anônimo NÃO pode registrar usuário (vetor de auto-elevação)")
    void anonimoNaoPodeRegistrar() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Hacker", "hacker@x.com", "Senha@123", "ADMIN")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("REGRESSÃO: FUNCIONARIO NÃO pode criar usuário ADMIN nem FUNCIONARIO")
    void funcionarioNaoPodeRegistrar() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", funcionarioToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Novo", "novo@x.com", "Senha@123", "FUNCIONARIO")))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("ADMIN pode criar usuário FUNCIONARIO")
    void adminPodeRegistrarFuncionario() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Novo Func", "novofunc@x.com", "Senha@123", "FUNCIONARIO")))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("REGRESSÃO: documentação da API não é pública")
    void swaggerNaoEPublico() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().is4xxClientError());
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("SEGURANÇA: usuário desativado NÃO consegue realizar login")
    void usuarioDesativadoNaoPodeLogar() throws Exception {
        Usuario func = usuarioRepository.findByEmail("func@reg.com").orElseThrow();
        func.setAtivo(false);
        usuarioRepository.save(func);

        String loginJson = """
                {
                  "email": "func@reg.com",
                  "senha": "Func@12345"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("SEGURANÇA: FUNCIONARIO não pode listar usuários internos")
    void funcionarioNaoPodeListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", funcionarioToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("SEGURANÇA: ADMIN pode listar usuários internos")
    void adminPodeListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header("Authorization", adminToken))
                .andExpect(status().isOk());
    }
}
