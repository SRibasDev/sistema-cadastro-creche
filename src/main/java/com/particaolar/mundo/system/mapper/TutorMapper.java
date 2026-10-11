package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.dto.request.TutorRequestDTO;
import com.particaolar.mundo.system.dto.response.TutorResponseDTO;
import com.particaolar.mundo.system.support.NormalizadorDados;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class TutorMapper {
    public Tutor toEntity(@NotNull TutorRequestDTO dto) {
        Tutor tutorEntity = new Tutor();
        tutorEntity.setNome(dto.nome());
        // Normaliza na persistão: garante forma canônica independente da entrada.
        tutorEntity.setTelefone(NormalizadorDados.normalizarTelefone(dto.telefone()));
        tutorEntity.setCpf(NormalizadorDados.normalizarCpf(dto.cpf()));
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
        tutor.setTelefone(NormalizadorDados.normalizarTelefone(dto.telefone()));
        tutor.setCpf(NormalizadorDados.normalizarCpf(dto.cpf()));
    }
}
