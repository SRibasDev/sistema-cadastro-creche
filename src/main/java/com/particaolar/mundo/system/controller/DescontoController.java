package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.DescontoService;
import com.particaolar.mundo.system.dto.request.DescontoRequestDTO;
import com.particaolar.mundo.system.dto.response.DescontoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Descontos e cupons. Escrita: ADMIN. Leitura: ambos os perfis.
 */
@RestController
@RequestMapping("/api/descontos")
@RequiredArgsConstructor
public class DescontoController {

    private final DescontoService descontoService;

    @PostMapping
    public ResponseEntity<DescontoResponseDTO> cadastrar(@Valid @RequestBody DescontoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(descontoService.salvar(dto));
    }

    @GetMapping
    public ResponseEntity<Page<DescontoResponseDTO>> listarTodos(
            @PageableDefault(size = 10, page = 0, sort = "codigo") Pageable pageable) {
        return ResponseEntity.ok(descontoService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DescontoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(descontoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DescontoResponseDTO> atualizar(@PathVariable Long id,
                                                         @Valid @RequestBody DescontoRequestDTO dto) {
        return ResponseEntity.ok(descontoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        descontoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
