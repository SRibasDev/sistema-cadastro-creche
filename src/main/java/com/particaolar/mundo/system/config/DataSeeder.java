package com.particaolar.mundo.system.config;

import com.particaolar.mundo.system.enums.Role;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default users on first startup if they don't exist.
 * Remove this class in production — use Flyway + manual provisioning.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            Usuario admin = new Usuario();
            admin.setNome("Administrador");
            admin.setEmail("admin@creche.com");
            admin.setSenha(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            usuarioRepository.save(admin);

            Usuario func = new Usuario();
            func.setNome("Funcionario");
            func.setEmail("func@creche.com");
            func.setSenha(passwordEncoder.encode("func123"));
            func.setRole(Role.FUNCIONARIO);
            usuarioRepository.save(func);

            log.info("Usuários padrão criados: admin@creche.com/admin123 | func@creche.com/func123");
        }
    }
}