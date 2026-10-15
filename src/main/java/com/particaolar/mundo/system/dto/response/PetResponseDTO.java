package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.Porte;

import java.time.LocalDate;

public record PetResponseDTO(
        Long id,
        String nome,
        String raca,
        LocalDate dataNascimento,
        Long tutorId,
        Porte porte,
        String especie,
        String sexo,
        String observacoes,
        String alergiasRestricoes,
        String vacinas
) {
    public PetResponseDTO(Long id, String nome, String raca, LocalDate dataNascimento, Long tutorId) {
        this(id, nome, raca, dataNascimento, tutorId, null, "CANINO", null, null, null, null);
    }
}
