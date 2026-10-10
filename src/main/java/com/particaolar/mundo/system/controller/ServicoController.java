package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.ServicoService;
import com.particaolar.mundo.system.dto.request.ServicoRequestDTO;
import com.particaolar.mundo.system.dto.response.ServicoResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Catálogo de serviços. Escrita: ADMIN. Leitura: ambos os perfis.
 */
@RestController
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    public ResponseEntity<ServicoResponseDTO> cadastrar(@Valid @RequestBody ServicoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(servicoService.salvar(dto));
    }

    @GetMapping
    public ResponseEntity<Page<ServicoResponseDTO>> listarTodos(
            @PageableDefault(size = 10, page = 0, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(servicoService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> atualizar(@PathVariable Long id,
                                                        @Valid @RequestBody ServicoRequestDTO dto) {
        return ResponseEntity.ok(servicoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        servicoService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
