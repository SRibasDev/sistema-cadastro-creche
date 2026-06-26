package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.TutorService;
import com.particaolar.mundo.system.dto.tutorDTO.TutorRequestDTO;
import com.particaolar.mundo.system.dto.tutorDTO.TutorResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutores")
@RequiredArgsConstructor
public class TutorController {

    private final TutorService tutorService;

    @PostMapping
    public ResponseEntity<TutorResponseDTO> cadastrar(@Validated @RequestBody TutorRequestDTO requestDTO) {
        TutorResponseDTO responseDTO = tutorService.salvarTutor(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<TutorResponseDTO>> listarTodos() {
        List<TutorResponseDTO> tutores = tutorService.listarTodos();
        return ResponseEntity.ok(tutores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TutorResponseDTO> buscarPorId(@PathVariable Long id) {
        TutorResponseDTO responseDTO = tutorService.buscarPorId(id);
        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{id}")
    public ResponseEntity <TutorResponseDTO> atualizar(@PathVariable Long id,@RequestBody @Validated TutorRequestDTO requestDTO){
        TutorResponseDTO tutor = tutorService.atualizar(requestDTO,id);
        return ResponseEntity.ok(tutor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id){
       tutorService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}