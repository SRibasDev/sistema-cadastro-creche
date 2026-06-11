package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.dto.PetRequestDTO;
import com.particaolar.mundo.system.dto.PetResponseDTO;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PetMapper {

    public Pet toEntity(@NonNull PetRequestDTO dto) {
        Pet petEntity = new Pet();
        petEntity.setNome(dto.nome());
        petEntity.setRaca(dto.raca());
        petEntity.setDataNascimento(dto.dataNascimento());
        return petEntity;
    }

    public PetResponseDTO toResponseDTO(@NonNull Pet petEntity) {
        return new PetResponseDTO(
                petEntity.getId(),
                petEntity.getNome(),
                petEntity.getRaca(),
                petEntity.getDataNascimento()
        );
    }
    public void updateEntityFromDTO(@NonNull PetRequestDTO dto, @NonNull Pet pet) {
        pet.setNome(dto.nome());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());
    }
}

