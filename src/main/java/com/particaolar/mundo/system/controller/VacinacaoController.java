package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.VacinacaoService;
import com.particaolar.mundo.system.dto.request.VacinacaoRequestDTO;
import com.particaolar.mundo.system.dto.response.VacinacaoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vacinacoes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
public class VacinacaoController {

    private final VacinacaoService vacinacaoService;

    @PostMapping("/pet/{petId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VacinacaoResponseDTO> registrar(
            @PathVariable Long petId,
            @RequestBody @Valid VacinacaoRequestDTO dto) {
        VacinacaoResponseDTO salva = vacinacaoService.registrar(petId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salva);
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<VacinacaoResponseDTO>> listarDoPet(@PathVariable Long petId) {
        return ResponseEntity.ok(vacinacaoService.listarDoPet(petId));
    }

    @GetMapping("/pet/{petId}/paginado")
    public ResponseEntity<Page<VacinacaoResponseDTO>> listarDoPetPaginado(
            @PathVariable Long petId,
            @PageableDefault(size = 10, page = 0, sort = "dataVencimento") Pageable pageable) {
        return ResponseEntity.ok(vacinacaoService.listarDoPetPaginado(petId, pageable));
    }

    @GetMapping("/pet/{petId}/vencidas")
    public ResponseEntity<List<VacinacaoResponseDTO>> listarVencidasDoPet(@PathVariable Long petId) {
        return ResponseEntity.ok(vacinacaoService.listarVencidasDoPet(petId));
    }

    @GetMapping("/vencidas")
    public ResponseEntity<List<VacinacaoResponseDTO>> listarVencidas() {
        return ResponseEntity.ok(vacinacaoService.listarVencidas());
    }

    @GetMapping("/a-vencer")
    public ResponseEntity<List<VacinacaoResponseDTO>> listarAVencer() {
        return ResponseEntity.ok(vacinacaoService.listarAVencer());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VacinacaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vacinacaoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VacinacaoResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid VacinacaoRequestDTO dto) {
        return ResponseEntity.ok(vacinacaoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> remover(@PathVariable Long id) {
        vacinacaoService.remover(id);
        return ResponseEntity.noContent().build();
    }
}
