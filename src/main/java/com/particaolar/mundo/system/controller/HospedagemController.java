package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.HospedagemService;
import com.particaolar.mundo.system.dto.hospedagemDTO.HospedagemRequestDTO;
import com.particaolar.mundo.system.dto.hospedagemDTO.HospedagemResponseDTO;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/hospedagens")
@RequiredArgsConstructor
public class HospedagemController {

    private final HospedagemService hospedagemService;

    @PostMapping
    public ResponseEntity<HospedagemResponseDTO> salvar(@RequestBody @Valid HospedagemRequestDTO dto) {
        HospedagemResponseDTO response = hospedagemService.salvar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<HospedagemResponseDTO>> listarTodas() {
        return ResponseEntity.ok(hospedagemService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HospedagemResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(hospedagemService.buscarPorId(id));
    }

    @GetMapping("/pet/{petId}")
    public ResponseEntity<List<HospedagemResponseDTO>> buscarPorPetId(@PathVariable Long petId) {
        return ResponseEntity.ok(hospedagemService.buscarPorPetId(petId));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<HospedagemResponseDTO> atualizarStatus(@PathVariable Long id, @RequestParam StatusHospedagem status) {
        return ResponseEntity.ok(hospedagemService.atualizarStatus(id, status));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HospedagemResponseDTO> atualizar(@PathVariable Long id, @RequestBody @Valid HospedagemRequestDTO dto) {
        return ResponseEntity.ok(hospedagemService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        hospedagemService.cancelar(id);
        return ResponseEntity.noContent().build();
    }
}