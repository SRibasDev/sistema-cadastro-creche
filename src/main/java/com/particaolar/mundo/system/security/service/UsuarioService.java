package com.particaolar.mundo.system.security.service;

import com.particaolar.mundo.system.enums.Role;
import com.particaolar.mundo.system.exception.UsuarioNotFoundException;
import com.particaolar.mundo.system.security.dto.TrocarSenhaRequestDTO;
import com.particaolar.mundo.system.security.dto.UsuarioResponseDTO;
import com.particaolar.mundo.system.security.dto.UsuarioUpdateRequestDTO;
import com.particaolar.mundo.system.security.entity.Usuario;
import com.particaolar.mundo.system.security.repository.UsuarioRepository;
import com.particaolar.mundo.system.support.NormalizadorDados;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private com.particaolar.mundo.system.domain.service.AuditLogService auditLogService;

    public UsuarioService(UsuarioRepository usuarioRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Mensagem genérica: não revela se o e-mail existe (anti-enumeração).
        return usuarioRepository.findByEmail(NormalizadorDados.normalizarEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return usuarioRepository.findAll(pageable).map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));
        return toDTO(usuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizar(Long id, UsuarioUpdateRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        String emailNorm = NormalizadorDados.normalizarEmail(dto.email());
        if (usuarioRepository.existsByEmailAndIdNot(emailNorm, id)) {
            throw new IllegalArgumentException("O e-mail informado já está em uso por outro usuário.");
        }

        if (usuario.getRole() == Role.ADMIN && dto.role() != Role.ADMIN) {
            long adminsAtivos = usuarioRepository.countByRoleAndAtivoTrue(Role.ADMIN);
            if (adminsAtivos <= 1) {
                throw new IllegalStateException("Não é permitido rebaixar o único administrador ativo do sistema.");
            }
        }

        usuario.setNome(dto.nome().trim());
        usuario.setEmail(emailNorm);
        usuario.setRole(dto.role());
        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário id={} atualizado para role={}", salvo.getId(), salvo.getRole());
        registrarAuditLog("ATUALIZAR_USUARIO", salvo.getId(), "Email: " + salvo.getEmail() + " Role: " + salvo.getRole());
        return toDTO(salvo);
    }

    @Transactional
    public UsuarioResponseDTO alterarStatus(Long id, boolean ativo, String solicitanteEmail) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        if (!ativo && usuario.getRole() == Role.ADMIN) {
            if (usuario.getEmail().equalsIgnoreCase(NormalizadorDados.normalizarEmail(solicitanteEmail))) {
                throw new IllegalStateException("Você não pode desativar sua própria conta.");
            }
            long adminsAtivos = usuarioRepository.countByRoleAndAtivoTrue(Role.ADMIN);
            if (adminsAtivos <= 1) {
                throw new IllegalStateException("Não é permitido desativar o único administrador ativo do sistema.");
            }
        }

        usuario.setAtivo(ativo);
        Usuario salvo = usuarioRepository.save(usuario);
        log.info("Usuário id={} teve status ativo alterado para {}", id, ativo);
        registrarAuditLog("ALTERAR_STATUS_USUARIO", id, "Status ativo alterado para " + ativo + " por " + solicitanteEmail);
        return toDTO(salvo);
    }

    @Transactional
    public void alterarSenha(Long id, TrocarSenhaRequestDTO dto, String solicitanteEmail, boolean isAdmin) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        boolean proprioUsuario = usuario.getEmail().equalsIgnoreCase(NormalizadorDados.normalizarEmail(solicitanteEmail));
        if (!isAdmin || proprioUsuario) {
            if (dto.senhaAtual() == null || dto.senhaAtual().isBlank()) {
                throw new IllegalArgumentException("A senha atual é obrigatória para alteração de senha.");
            }
            if (!passwordEncoder.matches(dto.senhaAtual(), usuario.getSenha())) {
                throw new IllegalArgumentException("Senha atual incorreta.");
            }
        }

        if (dto.novaSenha() == null || dto.novaSenha().length() < 8) {
            throw new IllegalArgumentException("A nova senha deve possuir no mínimo 8 caracteres.");
        }

        usuario.setSenha(passwordEncoder.encode(dto.novaSenha()));
        usuarioRepository.save(usuario);
        log.info("Senha do usuário id={} alterada com sucesso.", id);
        registrarAuditLog("ALTERAR_SENHA_USUARIO", id, "Senha alterada por " + solicitanteEmail);
    }

    private void registrarAuditLog(String acao, Long id, String detalhes) {
        if (auditLogService != null) {
            auditLogService.registrar(null, acao, "USUARIO", id, detalhes);
        }
    }

    public UsuarioResponseDTO toDTO(Usuario u) {
        return new UsuarioResponseDTO(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getRole(),
                u.getAtivo(),
                u.getCriadoEm()
        );
    }
}