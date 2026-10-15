package com.particaolar.mundo.system.dto.request;

import com.particaolar.mundo.system.enums.Porte;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PetRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "Raça é obrigatória")
        @Size(max = 50, message = "Raça deve ter no máximo 50 caracteres")
        String raca,

        LocalDate dataNascimento,

        Porte porte,

        @Size(max = 50, message = "Espécie deve ter no máximo 50 caracteres")
        String especie,

        @Size(max = 20, message = "Sexo deve ter no máximo 20 caracteres")
        String sexo,

        @Size(max = 500, message = "Observações deve ter no máximo 500 caracteres")
        String observacoes,

        @Size(max = 500, message = "Alergias e restrições deve ter no máximo 500 caracteres")
        String alergiasRestricoes,

        @Size(max = 500, message = "Vacinas deve ter no máximo 500 caracteres")
        String vacinas
) {
    public PetRequestDTO(String nome, String raca, LocalDate dataNascimento) {
        this(nome, raca, dataNascimento, null, "CANINO", null, null, null, null);
    }
}
