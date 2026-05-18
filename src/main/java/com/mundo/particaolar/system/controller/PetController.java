package com.mundo.particaolar.system.controller;

import com.mundo.particaolar.system.domain.service.PetService;
import com.mundo.particaolar.system.dto.PetRequestDTO;
import com.mundo.particaolar.system.dto.PetResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @PostMapping("/tutor/{tutorId}")
    public ResponseEntity<PetResponseDTO> cadastrar(@PathVariable Long tutorId, @RequestBody PetRequestDTO dto) {
    }
}
