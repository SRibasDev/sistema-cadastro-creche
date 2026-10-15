package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.AuditLog;
import com.particaolar.mundo.system.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrar(String usuario, String acao, String entidade, Long entidadeId, String detalhes) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setUsuario(obterUsuario(usuario));
            auditLog.setAcao(acao);
            auditLog.setEntidade(entidade);
            auditLog.setEntidadeId(entidadeId);
            auditLog.setDetalhes(detalhes);
            auditLog.setDataHora(LocalDateTime.now());
            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Erro ao registrar log de auditoria", e);
        }
    }

    private String obterUsuario(String usuario) {
        if (usuario != null && !usuario.isBlank()) {
            return usuario;
        }
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                return auth.getName();
            }
        } catch (Exception ignored) {
        }
        return "SISTEMA";
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> listarLogs(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return auditLogRepository.findAllByOrderByDataHoraDesc(pageable);
    }
}
