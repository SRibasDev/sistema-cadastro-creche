package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.AuditLog;
import com.particaolar.mundo.system.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
            auditLog.setUsuario(usuario != null ? usuario : "SYSTEM");
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

    @Transactional(readOnly = true)
    public Page<AuditLog> listarLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByDataHoraDesc(pageable);
    }
}
