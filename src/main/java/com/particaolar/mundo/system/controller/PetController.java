package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.PetService;
import com.particaolar.mundo.system.dto.request.PetRequestDTO;
import com.particaolar.mundo.system.dto.response.PetResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping("/{tutorId}")
    public ResponseEntity<PetResponseDTO> cadastrar(@PathVariable Long tutorId,  @Validated @RequestBody PetRequestDTO requestDTO) {
        PetResponseDTO responseDTO = petService.salvar(requestDTO,tutorId);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity <Page<PetResponseDTO>> listarTodos(@PageableDefault(size = 10, page = 0, sort = "nome") Pageable pageable){
       Page <PetResponseDTO> response = petService.listarTodos(pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping ("/{petId}")
    public ResponseEntity<PetResponseDTO> buscarPetPorPetId(@PathVariable Long petId){
        PetResponseDTO petResponseDTO = petService.buscarPetPorPetId(petId);
        return ResponseEntity.ok(petResponseDTO);
    }

    @GetMapping("/tutor/{tutorId}")
    public ResponseEntity<List<PetResponseDTO>> listarPorTutorId(@PathVariable Long tutorId){
       List<PetResponseDTO> pets = petService.buscarPetPorTutorId(tutorId);
       return ResponseEntity.ok(pets);
    }

    @PutMapping("/{petId}")
    public ResponseEntity<PetResponseDTO> atualizar(@PathVariable Long petId, @RequestBody @Validated PetRequestDTO requestDTO){
        PetResponseDTO pets = petService.atualizar(requestDTO,petId);
        return ResponseEntity.ok(pets);
    }

    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletar(@PathVariable Long petId){
        petService.deletar(petId);
        return ResponseEntity.noContent().build();
    }

}
