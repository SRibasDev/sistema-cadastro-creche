package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.PresencaService;
import com.particaolar.mundo.system.dto.response.ReservaResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Endpoints de presença usados pelo FUNCIONARIO:
 * consultar agendados, marcar entrada/saída/falta e "marcar todos presentes".
 * Acesso liberado a ADMIN e FUNCIONARIO em SecurityConfig.
 * SaaS: filtro por tenant_id entrará via JWT.
 */
@RestController
@RequestMapping("/api/presencas")
@RequiredArgsConstructor
public class PresencaController {

    private final PresencaService presencaService;

    /** Agendados de um dia (leitura). */
    @GetMapping("/agendados")
    public ResponseEntity<List<ReservaResponseDTO>> agendadosDoDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(presencaService.agendadosDoDia(data));
    }

    /** Marca entrada (pet chegou). */
    @PostMapping("/{reservaId}/entrada")
    public ResponseEntity<ReservaResponseDTO> marcarEntrada(@PathVariable Long reservaId) {
        return ResponseEntity.ok(presencaService.marcarEntrada(reservaId));
    }

    /** Marca saída (pet foi embora). */
    @PostMapping("/{reservaId}/saida")
    public ResponseEntity<ReservaResponseDTO> marcarSaida(@PathVariable Long reservaId) {
        return ResponseEntity.ok(presencaService.marcarSaida(reservaId));
    }

    /** Marca falta (pet não veio). */
    @PostMapping("/{reservaId}/falta")
    public ResponseEntity<ReservaResponseDTO> marcarFalta(@PathVariable Long reservaId) {
        return ResponseEntity.ok(presencaService.marcarFalta(reservaId));
    }

    /** Ação "marcar todos como presentes" do dia. */
    @PostMapping("/todos-presentes")
    public ResponseEntity<List<ReservaResponseDTO>> marcarTodosPresentes(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(presencaService.marcarTodosPresentes(data));
    }
}
