package com.particaolar.mundo.system.config;

import com.particaolar.mundo.system.enums.Role;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.repository.UsuarioRepository;
import com.particaolar.mundo.system.support.NormalizadorDados;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Provisionamento seguro do primeiro usuário ADMIN (dono da creche).
 *
 * <p><b>Regras de segurança:</b></p>
 * <ul>
 *   <li>Só é ativado quando {@code app.bootstrap-admin.enabled=true} (desligado por padrão).</li>
 *   <li>As credenciais vêm de variáveis de ambiente/propriedades — não há senha padrão no código.</li>
 *   <li>Recusa-se a criar um ADMIN com senha fraca (mínimo de 12 caracteres).</li>
 *   <li>Nunca registra a senha em log, apenas o e-mail (identificador não sensível).</li>
 *   <li>Cria o ADMIN apenas se ainda não existir nenhum usuário no sistema.</li>
 * </ul>
 *
 * <p>Fluxo recomendado: ligar uma única vez no primeiro deploy, criar o dono, trocar a senha
 * e desligar ({@code app.bootstrap-admin.enabled=false}). Em produção, os demais usuários
 * devem ser criados por um ADMIN autenticado via {@code POST /api/auth/register}.</p>
 */
@Component
@ConditionalOnProperty(name = "app.bootstrap-admin.enabled", havingValue = "true")
@RequiredArgsConstructor
@Slf4j
@Order(0)
public class AdminBootstrap implements ApplicationRunner {

    /** Tamanho mínimo aceito para a senha do primeiro ADMIN. */
    private static final int TAMANHO_MINIMO_SENHA = 12;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.email:}")
    private String email;

    @Value("${app.bootstrap-admin.password:}")
    private String password;

    @Value("${app.bootstrap-admin.nome:Administrador}")
    private String nome;

    /**
     * Cria o primeiro ADMIN se o banco estiver vazio e as credenciais forem válidas.
     * Não lança exceção para não derrubar a aplicação; apenas registra o problema e segue.
     */
    @Override
    public void run(ApplicationArguments args) {
        criarAdminInicial();
    }

    void criarAdminInicial() {
        if (usuarioRepository.count() > 0) {
            log.info("Bootstrap ADMIN ignorado: já existem usuários cadastrados.");
            return;
        }

        String emailNorm = NormalizadorDados.normalizarEmail(email);
        if (emailNorm == null || password == null || password.isBlank()) {
            log.error("Bootstrap ADMIN ativo mas APP_BOOTSTRAP_ADMIN_EMAIL/PASSWORD não definidos. "
                    + "Nenhum usuário foi criado. Defina as credenciais e reinicie.");
            return;
        }
        if (password.length() < TAMANHO_MINIMO_SENHA) {
            log.error("Bootstrap ADMIN recusado: a senha deve ter no mínimo {} caracteres.",
                    TAMANHO_MINIMO_SENHA);
            return;
        }

        Usuario admin = new Usuario();
        admin.setNome(nome == null || nome.isBlank() ? "Administrador" : nome.trim());
        admin.setEmail(emailNorm);
        admin.setSenha(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        usuarioRepository.save(admin);

        // Registra apenas o identificador; a senha NUNCA vai para o log.
        log.info("Primeiro ADMIN provisionado com sucesso para '{}'.", emailNorm);
    }
}
