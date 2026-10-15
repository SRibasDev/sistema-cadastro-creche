package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.DashboardService;
import com.particaolar.mundo.system.dto.response.DashboardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public ResponseEntity<DashboardDTO> obterMetricas() {
        return ResponseEntity.ok(dashboardService.obterMetricas());
    }
}
