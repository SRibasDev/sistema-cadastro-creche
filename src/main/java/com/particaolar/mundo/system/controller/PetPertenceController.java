package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.PetPertenceService;
import com.particaolar.mundo.system.dto.request.PetPertenceRequestDTO;
import com.particaolar.mundo.system.dto.response.PetPertenceResponseDTO;
import com.particaolar.mundo.system.enums.TipoPertence;
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
 * Pertences do cão. Escrita: ADMIN. Leitura: ambos os perfis.
 */
@RestController
@RequestMapping("/api/pets/{petId}/pertences")
@RequiredArgsConstructor
public class PetPertenceController {

    private final PetPertenceService petPertenceService;

    @PostMapping
    public ResponseEntity<PetPertenceResponseDTO> cadastrar(
            @PathVariable Long petId, @Valid @RequestBody PetPertenceRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(petPertenceService.salvar(petId, dto));
    }

    @GetMapping
    public ResponseEntity<List<PetPertenceResponseDTO>> listar(@PathVariable Long petId) {
        return ResponseEntity.ok(petPertenceService.listarPorPet(petId));
    }

    @GetMapping("/paginado")
    public ResponseEntity<Page<PetPertenceResponseDTO>> listarPaginado(
            @PathVariable Long petId,
            @PageableDefault(size = 10, page = 0, sort = "tipo") Pageable pageable) {
        return ResponseEntity.ok(petPertenceService.listarPaginado(petId, pageable));
    }

    @GetMapping("/tipo/{tipo}")
    public ResponseEntity<List<PetPertenceResponseDTO>> listarPorTipo(
            @PathVariable Long petId, @PathVariable TipoPertence tipo) {
        return ResponseEntity.ok(petPertenceService.listarPorPetETipo(petId, tipo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PetPertenceResponseDTO> atualizar(
            @PathVariable Long petId, @PathVariable Long id,
            @Valid @RequestBody PetPertenceRequestDTO dto) {
        return ResponseEntity.ok(petPertenceService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long petId, @PathVariable Long id) {
        petPertenceService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
