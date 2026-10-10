package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.PacoteService;
import com.particaolar.mundo.system.dto.request.PacoteCompraRequestDTO;
import com.particaolar.mundo.system.dto.request.PacoteRequestDTO;
import com.particaolar.mundo.system.dto.response.PacoteCompradoResponseDTO;
import com.particaolar.mundo.system.dto.response.PacoteResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pacotes/combos e compras (saldo). Escrita: ADMIN. Leitura: ambos os perfis.
 */
@RestController
@RequestMapping("/api/pacotes")
@RequiredArgsConstructor
public class PacoteController {

    private final PacoteService pacoteService;

    @PostMapping
    public ResponseEntity<PacoteResponseDTO> cadastrar(@Valid @RequestBody PacoteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacoteService.salvar(dto));
    }

    @GetMapping
    public ResponseEntity<Page<PacoteResponseDTO>> listarTodos(
            @PageableDefault(size = 10, page = 0, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(pacoteService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacoteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(pacoteService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacoteResponseDTO> atualizar(@PathVariable Long id,
                                                       @Valid @RequestBody PacoteRequestDTO dto) {
        return ResponseEntity.ok(pacoteService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        pacoteService.deletar(id);
        return ResponseEntity.noContent().build();
    }

    /** Compra de pacote para um pet (gera saldo com validade). */
    @PostMapping("/{id}/compras")
    public ResponseEntity<PacoteCompradoResponseDTO> comprar(
            @PathVariable Long id, @Valid @RequestBody PacoteCompraRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacoteService.comprar(id, dto));
    }

    /** Saldos (pacotes comprados) de um pet. */
    @GetMapping("/saldos/pet/{petId}")
    public ResponseEntity<List<PacoteCompradoResponseDTO>> saldosPorPet(@PathVariable Long petId) {
        return ResponseEntity.ok(pacoteService.saldosPorPet(petId));
    }
}
