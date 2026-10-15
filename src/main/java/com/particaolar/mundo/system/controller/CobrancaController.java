package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.CobrancaService;
import com.particaolar.mundo.system.dto.request.CobrancaRequestDTO;
import com.particaolar.mundo.system.dto.request.PagamentoRequestDTO;
import com.particaolar.mundo.system.dto.response.CobrancaResponseDTO;
import com.particaolar.mundo.system.dto.response.PagamentoResponseDTO;
import com.particaolar.mundo.system.enums.StatusCobranca;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cobrancas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
public class CobrancaController {

    private final CobrancaService cobrancaService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CobrancaResponseDTO> criar(
            @RequestBody @Valid CobrancaRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cobrancaService.criar(dto, authentication.getName()));
    }

    @GetMapping
    public ResponseEntity<Page<CobrancaResponseDTO>> listar(
            @RequestParam(required = false) StatusCobranca status,
            @PageableDefault(size = 10, page = 0, sort = "dataVencimento") Pageable pageable) {
        return ResponseEntity.ok(cobrancaService.listar(pageable, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CobrancaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cobrancaService.buscarPorId(id));
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<CobrancaResponseDTO>> listarPorTutor(@PathVariable Long tutorId) {
        return ResponseEntity.ok(cobrancaService.listarPorTutor(tutorId));
    }

    @GetMapping("/hospedagem/{hospedagemId}")
    public ResponseEntity<List<CobrancaResponseDTO>> listarPorHospedagem(@PathVariable Long hospedagemId) {
        return ResponseEntity.ok(cobrancaService.listarPorHospedagem(hospedagemId));
    }

    @PostMapping("/{id}/pagamentos")
    public ResponseEntity<PagamentoResponseDTO> registrarPagamento(
            @PathVariable Long id,
            @RequestBody @Valid PagamentoRequestDTO dto,
            Authentication authentication) {
        PagamentoRequestDTO dtoConsolidado = new PagamentoRequestDTO(
                id,
                dto.valor(),
                dto.formaPagamento(),
                dto.comprovante(),
                dto.observacoes()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cobrancaService.registrarPagamento(dtoConsolidado, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelar(
            @PathVariable Long id,
            @RequestParam(required = false) String motivo,
            Authentication authentication) {
        cobrancaService.cancelar(id, motivo, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
