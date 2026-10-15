package com.particaolar.mundo.system.security.controller;

import com.particaolar.mundo.system.security.dto.TrocarSenhaRequestDTO;
import com.particaolar.mundo.system.security.dto.UsuarioResponseDTO;
import com.particaolar.mundo.system.security.dto.UsuarioUpdateRequestDTO;
import com.particaolar.mundo.system.security.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UsuarioResponseDTO>> listarTodos(
            @PageableDefault(size = 10, page = 0, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.listarTodos(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> atualizar(
            @PathVariable Long id,
            @RequestBody @Valid UsuarioUpdateRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.atualizar(id, dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> alterarStatus(
            @PathVariable Long id,
            @RequestParam boolean ativo,
            Authentication authentication) {
        return ResponseEntity.ok(usuarioService.alterarStatus(id, ativo, authentication.getName()));
    }

    @PatchMapping("/{id}/senha")
    public ResponseEntity<Void> alterarSenha(
            @PathVariable Long id,
            @RequestBody @Valid TrocarSenhaRequestDTO dto,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        usuarioService.alterarSenha(id, dto, authentication.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
