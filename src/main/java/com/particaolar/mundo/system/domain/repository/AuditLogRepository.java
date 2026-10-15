package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntidadeAndEntidadeIdOrderByDataHoraDesc(String entidade, Long entidadeId);
    Page<AuditLog> findAllByOrderByDataHoraDesc(Pageable pageable);
}
