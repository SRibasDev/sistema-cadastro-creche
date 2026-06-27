package com.particaolar.mundo.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PetRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 30, message = "Nome deve ter no máximo 30 caracteres")
        String nome,
        @NotBlank(message = "Raça é obrigatório")
        String raca,
        LocalDate dataNascimento
)
{}
