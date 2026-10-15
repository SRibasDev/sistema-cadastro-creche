package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.RelatorioService;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import com.particaolar.mundo.system.dto.response.RelatorioFinanceiroDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/financeiro")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RelatorioFinanceiroDTO> relatorioFinanceiro(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.gerarRelatorioFinanceiro(inicio, fim));
    }

    @GetMapping("/reservas")
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
    public ResponseEntity<List<HospedagemResponseDTO>> relatorioReservas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(relatorioService.gerarRelatorioReservas(inicio, fim));
    }
}
