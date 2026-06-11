package com.particaolar.mundo.system.dto;

import java.time.LocalDate;

public record PetResponseDTO(Long id, String nome, String raca, LocalDate dataNascimento) {
}
