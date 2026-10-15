package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.ServicoService;
import com.particaolar.mundo.system.dto.request.ServicoRequestDTO;
import com.particaolar.mundo.system.dto.response.ServicoResponseDTO;
import com.particaolar.mundo.system.enums.TipoServico;
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
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicoResponseDTO> salvar(@RequestBody @Valid ServicoRequestDTO dto) {
        ServicoResponseDTO salvo = servicoService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(salvo);
    }

    @GetMapping
    public ResponseEntity<Page<ServicoResponseDTO>> listarAtivos(
            @PageableDefault(size = 10, page = 0, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(servicoService.listarAtivos(pageable));
    }

    @GetMapping("/lista")
    public ResponseEntity<List<ServicoResponseDTO>> listarTodosAtivos() {
        return ResponseEntity.ok(servicoService.listarTodosAtivos());
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<ServicoResponseDTO>> buscarPorTipo(@PathVariable TipoServico tipo) {
        return ResponseEntity.ok(servicoService.buscarPorTipo(tipo));
    }

    @GetMapping("/todos")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<ServicoResponseDTO>> listarTodos(
            @PageableDefault(size = 10, page = 0, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(servicoService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServicoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(servicoService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicoResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid ServicoRequestDTO dto) {
        return ResponseEntity.ok(servicoService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> alterarStatus(
            @PathVariable Long id,
            @RequestParam boolean ativo) {
        servicoService.alterarStatus(id, ativo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        servicoService.alterarStatus(id, false);
        return ResponseEntity.noContent().build();
    }
}
