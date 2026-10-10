package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.PetPertence;
import com.particaolar.mundo.system.dto.request.PetPertenceRequestDTO;
import com.particaolar.mundo.system.dto.response.PetPertenceResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class PetPertenceMapper {

    public PetPertence toEntity(PetPertenceRequestDTO dto) {
        PetPertence pertence = new PetPertence();
        pertence.setTipo(dto.tipo());
        pertence.setDescricao(dto.descricao());
        pertence.setQuantidade(dto.quantidade());
        pertence.setQuantidadeNumero(dto.quantidadeNumero());
        pertence.setObservacoes(dto.observacoes());
        return pertence;
    }

    public PetPertenceResponseDTO toResponseDTO(PetPertence pertence) {
        return new PetPertenceResponseDTO(
                pertence.getId(),
                pertence.getPet().getId(),
                pertence.getTipo(),
                pertence.getDescricao(),
                pertence.getQuantidade(),
                pertence.getQuantidadeNumero(),
                pertence.getObservacoes(),
                pertence.getAtivo(),
                pertence.getCriadoEm()
        );
    }

    public void updateEntityFromDTO(PetPertenceRequestDTO dto, PetPertence pertence) {
        pertence.setTipo(dto.tipo());
        pertence.setDescricao(dto.descricao());
        pertence.setQuantidade(dto.quantidade());
        pertence.setQuantidadeNumero(dto.quantidadeNumero());
        pertence.setObservacoes(dto.observacoes());
    }
}
