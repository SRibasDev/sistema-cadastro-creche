package com.particaolar.mundo.system.dto.response;

import java.time.LocalDate;

public record PetResponseDTO(Long id, String nome, String raca, LocalDate dataNascimento, Long tutorId) {
}
