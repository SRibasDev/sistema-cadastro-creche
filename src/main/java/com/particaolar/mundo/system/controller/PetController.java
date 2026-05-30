package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.service.PetService;
import com.particaolar.mundo.system.dto.PetRequestDTO;
import com.particaolar.mundo.system.dto.PetResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pets")
@RequiredArgsConstructor
public class PetController {

    private final PetService petService;

}
