package com.mundo.particaolar.system.mapper;

import com.mundo.particaolar.system.controller.PetController;
import com.mundo.particaolar.system.domain.entity.Pet;
import com.mundo.particaolar.system.dto.PetRequestDTO;
import com.mundo.particaolar.system.dto.PetResponseDTO;

public class PetMapper {

    public static Pet toEntity(PetRequestDTO dto) {
        Pet petEntity = new Pet();
        petEntity.setNome(dto.nome());
        petEntity.setRaca(dto.raca());
        petEntity.setIdade(dto.idade());
        return petEntity;
    }

    public static PetResponseDTO toResponseDTO(Pet petEntity) {
        return new PetResponseDTO(
                petEntity.getId(),
                petEntity.getNome(),
                petEntity.getRaca(),
                petEntity.getIdade()
        );
    }
}