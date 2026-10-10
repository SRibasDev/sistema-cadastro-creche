package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.dto.request.PetRequestDTO;
import com.particaolar.mundo.system.dto.response.PetResponseDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class PetMapper {

    public Pet toEntity(@NotNull PetRequestDTO dto) {
        Pet petEntity = new Pet();
        petEntity.setNome(dto.nome());
        petEntity.setRaca(dto.raca());
        petEntity.setDataNascimento(dto.dataNascimento());
        petEntity.setAlertas(dto.alertas());
        petEntity.setObservacoesGerais(dto.observacoesGerais());
        return petEntity;
    }

    public PetResponseDTO toResponseDTO(@NotNull Pet petEntity) {
        return new PetResponseDTO(
                petEntity.getId(),
                petEntity.getNome(),
                petEntity.getRaca(),
                petEntity.getDataNascimento(),
                petEntity.getTutor().getId(),
                petEntity.getAlertas(),
                petEntity.getObservacoesGerais()
        );
    }
    public void updateEntityFromDTO(@NotNull PetRequestDTO dto, @NotNull Pet pet) {
        pet.setNome(dto.nome());
        pet.setRaca(dto.raca());
        pet.setDataNascimento(dto.dataNascimento());
        pet.setAlertas(dto.alertas());
        pet.setObservacoesGerais(dto.observacoesGerais());
    }
}

