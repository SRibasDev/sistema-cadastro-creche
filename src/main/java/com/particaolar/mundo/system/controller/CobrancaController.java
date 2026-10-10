package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.CobrancaService;
import com.particaolar.mundo.system.dto.request.CobrancaRequestDTO;
import com.particaolar.mundo.system.dto.response.CobrancaResponseDTO;
import com.particaolar.mundo.system.enums.StatusCobranca;
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

/**
 * Cobranças. Escrita: ADMIN. Leitura: ambos os perfis.
 * Pagamento é registrado manualmente nesta fase (sem gateway).
 */
@RestController
@RequestMapping("/api/cobrancas")
@RequiredArgsConstructor
public class CobrancaController {

    private final CobrancaService cobrancaService;

    @PostMapping
    public ResponseEntity<CobrancaResponseDTO> gerar(@Valid @RequestBody CobrancaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cobrancaService.salvar(dto));
    }

    @GetMapping
    public ResponseEntity<Page<CobrancaResponseDTO>> listarTodas(
            @PageableDefault(size = 10, page = 0, sort = "dataVencimento") Pageable pageable) {
        return ResponseEntity.ok(cobrancaService.listarTodas(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CobrancaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cobrancaService.buscarPorId(id));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<Page<CobrancaResponseDTO>> listarPorStatus(
            @PathVariable StatusCobranca status,
            @PageableDefault(size = 10, page = 0, sort = "dataVencimento") Pageable pageable) {
        return ResponseEntity.ok(cobrancaService.listarPorStatus(status, pageable));
    }

    /** Registra pagamento manual: PATCH /api/cobrancas/{id}/pagamento */
    @PatchMapping("/{id}/pagamento")
    public ResponseEntity<CobrancaResponseDTO> registrarPagamento(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataPagamento) {
        return ResponseEntity.ok(cobrancaService.registrarPagamento(id, dataPagamento));
    }

    /** Cancela cobrança: DELETE /api/cobrancas/{id}?motivo=... */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id,
                                         @RequestParam(required = false) String motivo) {
        cobrancaService.cancelar(id, motivo);
        return ResponseEntity.noContent().build();
    }
}
