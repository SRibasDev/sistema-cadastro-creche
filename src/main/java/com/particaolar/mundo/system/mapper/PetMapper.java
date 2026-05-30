package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.dto.PetRequestDTO;
import com.particaolar.mundo.system.dto.PetResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class PetMapper {

    public Pet toEntity(PetRequestDTO dto) {
        Pet petEntity = new Pet();
        petEntity.setNome(dto.nome());
        petEntity.setRaca(dto.raca());
        petEntity.setDataNascimento(dto.dataNascimento());
        return petEntity;
    }

    public PetResponseDTO toResponseDTO(Pet petEntity) {
        return new PetResponseDTO(
                petEntity.getId(),
                petEntity.getNome(),
                petEntity.getRaca(),
                petEntity.getDataNascimento()
        );
    }
}

