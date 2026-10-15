package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.entity.AuditLog;
import com.particaolar.mundo.system.domain.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<Page<AuditLog>> listar(
            @PageableDefault(size = 20, page = 0) Pageable pageable) {
        return ResponseEntity.ok(auditLogService.listarLogs(pageable));
    }
}
