package com.particaolar.mundo.system.dto.petDTO;

import java.time.LocalDate;

public record PetResponseDTO(Long id, String nome, String raca, LocalDate dataNascimento, Long tutorId) {
}
