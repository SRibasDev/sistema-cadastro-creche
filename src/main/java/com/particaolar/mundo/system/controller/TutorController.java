package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.TutorService;
import com.particaolar.mundo.system.dto.request.TutorRequestDTO;
import com.particaolar.mundo.system.dto.response.TutorResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tutores")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'FUNCIONARIO')")
public class TutorController {

    private final TutorService tutorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TutorResponseDTO> cadastrar(@Validated @RequestBody TutorRequestDTO requestDTO) {
        TutorResponseDTO responseDTO = tutorService.salvarTutor(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<Page<TutorResponseDTO>> listarTodos(@PageableDefault(size = 10, page = 0, sort = "nome") Pageable pageable) {
            Page<TutorResponseDTO> response = tutorService.listarTodos(pageable);
            return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorResponseDTO> buscarPorId(@PathVariable Long id) {
        TutorResponseDTO responseDTO = tutorService.buscarPorId(id);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity <TutorResponseDTO> atualizar(@PathVariable Long id,@RequestBody @Validated TutorRequestDTO requestDTO){
        TutorResponseDTO tutor = tutorService.atualizar(requestDTO,id);
        return ResponseEntity.ok(tutor);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
       tutorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}