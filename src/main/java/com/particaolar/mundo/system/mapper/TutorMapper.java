package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.dto.TutorRequestDTO;
import com.particaolar.mundo.system.dto.TutorResponseDTO;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TutorMapper {
    public Tutor toEntity(@NonNull TutorRequestDTO dto) {
        Tutor tutorEntity = new Tutor();
        tutorEntity.setNome(dto.nome());
        tutorEntity.setTelefone(dto.telefone());
        tutorEntity.setCpf(dto.cpf());
        return tutorEntity;
    }

    public TutorResponseDTO toResponseDTO(@NonNull Tutor tutorEntity) {
        return new TutorResponseDTO(
                tutorEntity.getId(),
                tutorEntity.getNome(),
                tutorEntity.getTelefone()
        );
    }

    public void updateEntitiyFromDTO(@NotNull TutorRequestDTO dto,@NotNull Tutor tutor){
        tutor.setNome(dto.nome());
        tutor.setTelefone(dto.telefone());
        tutor.setCpf(dto.cpf());
    }
}
