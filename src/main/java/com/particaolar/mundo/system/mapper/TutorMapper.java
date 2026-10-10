package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.dto.request.TutorRequestDTO;
import com.particaolar.mundo.system.dto.response.TutorResponseDTO;
import com.particaolar.mundo.system.util.Documentos;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class TutorMapper {
    public Tutor toEntity(@NotNull TutorRequestDTO dto) {
        Tutor tutorEntity = new Tutor();
        tutorEntity.setNome(dto.nome());
        tutorEntity.setTelefone(Documentos.normalizarTelefone(dto.telefone()));
        tutorEntity.setCpf(Documentos.normalizarCpf(dto.cpf()));
        return tutorEntity;
    }

    public TutorResponseDTO toResponseDTO(@NotNull Tutor tutorEntity) {
        return new TutorResponseDTO(
                tutorEntity.getId(),
                tutorEntity.getNome(),
                tutorEntity.getTelefone()
        );
    }
    public void updateEntityFromDTO(@NotNull TutorRequestDTO dto,@NotNull Tutor tutor){
        tutor.setNome(dto.nome());
        tutor.setTelefone(Documentos.normalizarTelefone(dto.telefone()));
        tutor.setCpf(Documentos.normalizarCpf(dto.cpf()));
    }
}
