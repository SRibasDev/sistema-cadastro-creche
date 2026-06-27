package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.dto.request.HospedagemRequestDTO;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class HospedagemMapper {

    public Hospedagem toEntity(HospedagemRequestDTO dto) {
        Hospedagem hospedagem = new Hospedagem();
        hospedagem.setDataEntrada(dto.dataEntrada());
        hospedagem.setDataSaida(dto.dataSaida());
        hospedagem.setObservacoes(dto.observacoes());
        return hospedagem;
    }

    public HospedagemResponseDTO toResponseDTO(Hospedagem hospedagem) {
        return new HospedagemResponseDTO(
                hospedagem.getId(),
                hospedagem.getPet().getId(),
                hospedagem.getPet().getNome(),
                hospedagem.getPet().getTutor().getNome(),
                hospedagem.getDataEntrada(),
                hospedagem.getDataSaida(),
                hospedagem.getStatus(),
                hospedagem.getObservacoes(),
                hospedagem.getCriadoEm()
        );
    }

    public void updateEntityFromDTO(HospedagemRequestDTO dto, Hospedagem hospedagem) {
        hospedagem.setDataEntrada(dto.dataEntrada());
        hospedagem.setDataSaida(dto.dataSaida());
        hospedagem.setObservacoes(dto.observacoes());
    }
}