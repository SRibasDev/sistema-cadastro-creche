package com.particaolar.mundo.system.config;

import com.particaolar.mundo.system.enums.Role;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds default users on first startup if they don't exist.
 * ACTIVE ONLY in the "dev" profile and when seed.default-users.enabled=true.
 * Never runs in production — provision users via /api/auth/usuarios (ADMIN).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment environment;

    @Override
    public void run(String... args) {
        if (!seedingEnabled()) {
            return;
        }
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

            log.info("Usuários padrão (dev) criados: admin@creche.com | func@creche.com");
        }
    }

    private boolean seedingEnabled() {
        boolean devProfile = environment.acceptsProfiles(org.springframework.core.env.Profiles.of("dev"));
        if (!devProfile) {
            log.info("DataSeeder desabilitado (perfis ativos: {}). Use /api/auth/usuarios para criar usuários.",
                    String.join(",", environment.getActiveProfiles()));
            return false;
        }
        return environment.getProperty("seed.default-users.enabled", Boolean.class, true);
    }
}