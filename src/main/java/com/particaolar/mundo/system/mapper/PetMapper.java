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
        petEntity.setNome(dto.nome().trim());
        petEntity.setRaca(dto.raca().trim());
        petEntity.setDataNascimento(dto.dataNascimento());
        petEntity.setPorte(dto.porte());
        petEntity.setEspecie(dto.especie() != null && !dto.especie().isBlank() ? dto.especie().trim() : "CANINO");
        petEntity.setSexo(dto.sexo() != null ? dto.sexo().trim() : null);
        petEntity.setObservacoes(dto.observacoes() != null ? dto.observacoes().trim() : null);
        petEntity.setAlergiasRestricoes(dto.alergiasRestricoes() != null ? dto.alergiasRestricoes().trim() : null);
        petEntity.setVacinas(dto.vacinas() != null ? dto.vacinas().trim() : null);
        return petEntity;
    }

    public PetResponseDTO toResponseDTO(@NotNull Pet petEntity) {
        return new PetResponseDTO(
                petEntity.getId(),
                petEntity.getNome(),
                petEntity.getRaca(),
                petEntity.getDataNascimento(),
                petEntity.getTutor() != null ? petEntity.getTutor().getId() : null,
                petEntity.getPorte(),
                petEntity.getEspecie(),
                petEntity.getSexo(),
                petEntity.getObservacoes(),
                petEntity.getAlergiasRestricoes(),
                petEntity.getVacinas()
        );
    }

    public void updateEntityFromDTO(@NotNull PetRequestDTO dto, @NotNull Pet pet) {
        pet.setNome(dto.nome().trim());
        pet.setRaca(dto.raca().trim());
        pet.setDataNascimento(dto.dataNascimento());
        if (dto.porte() != null) pet.setPorte(dto.porte());
        if (dto.especie() != null) pet.setEspecie(dto.especie().trim());
        if (dto.sexo() != null) pet.setSexo(dto.sexo().trim());
        if (dto.observacoes() != null) pet.setObservacoes(dto.observacoes().trim());
        if (dto.alergiasRestricoes() != null) pet.setAlergiasRestricoes(dto.alergiasRestricoes().trim());
        if (dto.vacinas() != null) pet.setVacinas(dto.vacinas().trim());
    }
}
