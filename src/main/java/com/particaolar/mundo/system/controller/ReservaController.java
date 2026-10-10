package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.ReservaService;
import com.particaolar.mundo.system.dto.request.ReagendarRequestDTO;
import com.particaolar.mundo.system.dto.request.ReservaRequestDTO;
import com.particaolar.mundo.system.dto.response.ReservaResponseDTO;
import com.particaolar.mundo.system.dto.response.ReservaHistoricoResponseDTO;
import com.particaolar.mundo.system.enums.StatusReserva;
import com.particaolar.mundo.system.exception.ReservaNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reservas de CRECHE/HOTEL (substitui /api/hospedagens).
 * Escrita: ADMIN. Leitura: ADMIN e FUNCIONARIO (regras em SecurityConfig).
 * SaaS: GET /dia e /dia/resumo receberão tenant_id pelo JWT no futuro.
 */
@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> salvar(@RequestBody @Valid ReservaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservaService.salvar(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ReservaResponseDTO>> listarTodas(
            @PageableDefault(size = 10, page = 0, sort = "dataEntrada") Pageable pageable) {
        return ResponseEntity.ok(reservaService.listarTodas(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<ReservaResponseDTO>> buscarPorPetId(@PathVariable Long petId) {
        return ResponseEntity.ok(reservaService.buscarPorPetId(petId));
    }

    /** Visão do dia: quem está agendado em uma data. */
    @GetMapping("/dia")
    public ResponseEntity<List<ReservaResponseDTO>> buscarPorDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(reservaService.buscarPorDia(data));
    }

    /** Lotação do dia por tipo (ocupadas/capacidade/livres). */
    @GetMapping("/dia/resumo")
    public ResponseEntity<Map<String, Object>> resumoDoDia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data) {
        return ResponseEntity.ok(reservaService.resumoDoDia(data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> atualizar(@PathVariable Long id,
                                                        @RequestBody @Valid ReservaRequestDTO dto) {
        return ResponseEntity.ok(reservaService.atualizar(id, dto));
    }

    /** Reagendamento com histórico e motivo. */
    @PostMapping("/{id}/reagendar")
    public ResponseEntity<ReservaResponseDTO> reagendar(@PathVariable Long id,
                                                        @RequestBody @Valid ReagendarRequestDTO dto) {
        return ResponseEntity.ok(reservaService.reagendar(id, dto));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ReservaResponseDTO> atualizarStatus(@PathVariable Long id,
                                                              @RequestParam StatusReserva status) {
        return ResponseEntity.ok(reservaService.atualizarStatus(id, status));
    }

    /** Cancelamento lógico com motivo: DELETE /api/reservas/{id}?motivo=... */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id,
                                         @RequestParam(required = false) String motivo) {
        reservaService.cancelar(id, motivo);
        return ResponseEntity.noContent().build();
    }

    /** Histórico de eventos de uma reserva (reagendamentos, cancelamentos...). */
    @GetMapping("/{id}/historicos")
    public ResponseEntity<List<ReservaHistoricoResponseDTO>> historicos(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarHistoricos(id));
    }
}
