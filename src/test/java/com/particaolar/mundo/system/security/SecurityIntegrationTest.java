package com.particaolar.mundo.system.security;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.enums.Role;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.repository.UsuarioRepository;
import com.particaolar.mundo.system.security.service.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "jwt.secret=test_secret_key_for_integration_tests_only_min_32_chars_long",
        "jwt.expiration=3600000",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TutorRepository tutorRepository;
    @Autowired private PetRepository petRepository;
    @Autowired private HospedagemRepository hospedagemRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtService jwtService;

    private String adminToken;
    private String funcionarioToken;
    private Long tutorId;
    private Long petId;
@BeforeEach
    void setUp() {
        hospedagemRepository.deleteAll();
        petRepository.deleteAll();
        tutorRepository.deleteAll();
        usuarioRepository.deleteAll();

        Usuario admin = new Usuario();
        admin.setNome("Admin"); admin.setEmail("admin@test.com");
        admin.setSenha(passwordEncoder.encode("admin123456"));
        admin.setRole(Role.ADMIN);
        usuarioRepository.save(admin);
        adminToken = "Bearer " + jwtService.gerarToken(admin);

        Usuario func = new Usuario();
        func.setNome("Func"); func.setEmail("func@test.com");
        func.setSenha(passwordEncoder.encode("func123456"));
        func.setRole(Role.FUNCIONARIO);
        usuarioRepository.save(func);
        funcionarioToken = "Bearer " + jwtService.gerarToken(func);

        Tutor tutor = new Tutor();
        tutor.setNome("Tutor Teste"); tutor.setTelefone("11988887777");
        tutor.setCpf("529.982.247-25"); tutor.setAtivo(true);
        tutor.setCriadoEm(java.time.LocalDateTime.now());
        tutor.setAtualizadoEm(java.time.LocalDateTime.now());
        tutor = tutorRepository.save(tutor);
        tutorId = tutor.getId();

        Pet pet = new Pet();
        pet.setNome("Rex"); pet.setRaca("Labrador");
        pet.setDataNascimento(LocalDate.of(2020, 1, 1));
        pet.setTutor(tutor); pet.setAtivo(true);
        pet.setCriadoEm(java.time.LocalDateTime.now());
        pet.setAtualizadoEm(java.time.LocalDateTime.now());
        pet = petRepository.save(pet);
        petId = pet.getId();
    }

    @Test
    void deveRejeitarRequisicaoSemToken() throws Exception {
        mockMvc.perform(get("/api/tutores"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/tutores")
                        .header("Authorization", "Bearer token_invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRejeitarTokenExpirado() throws Exception {
        SecretKey key = Keys.hmacShaKeyFor(
                "test_secret_key_for_integration_tests_only_min_32_chars_long"
                        .getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("admin@test.com")
                .issuedAt(new Date(System.currentTimeMillis() - 7200000))
                .expiration(new Date(System.currentTimeMillis() - 3600000))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
        mockMvc.perform(get("/api/tutores")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void funcionarioNaoPodeDeletar() throws Exception {
        mockMvc.perform(delete("/api/tutores/" + tutorId)
                        .header("Authorization", funcionarioToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void naoDeveAcessarTutorInativo() throws Exception {
        Tutor tutor = tutorRepository.findById(tutorId).orElseThrow();
        tutor.setAtivo(false);
        tutorRepository.save(tutor);
        mockMvc.perform(get("/api/tutores/" + tutorId)
                        .header("Authorization", adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveRejeitarTutorSemNome() throws Exception {
        String json = """
                {
                    "nome": "",
                    "telefone": "11977776666",
                    "cpf": "798.056.630-62"
                }""";
        mockMvc.perform(post("/api/tutores")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarCpfInvalido() throws Exception {
        String json = """
                {
                    "nome": "Tutor",
                    "telefone": "11977776666",
                    "cpf": "111.111.111-11"
                }""";
        mockMvc.perform(post("/api/tutores")
                        .header("Authorization", adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveRejeitarPaginaMuitoGrande() throws Exception {
        mockMvc.perform(get("/api/tutores?size=1000&page=0")
                        .header("Authorization", adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginComCredenciaisInvalidasRetorna401() throws Exception {
        String json = """
                {
                    "email": "admin@test.com",
                    "senha": "senha_errada"
                }""";
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}