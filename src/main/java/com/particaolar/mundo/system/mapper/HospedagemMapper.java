package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.dto.request.HospedagemRequestDTO;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class HospedagemMapper {

    public Hospedagem toEntity(HospedagemRequestDTO dto) {
        Hospedagem hospedagem = new Hospedagem();
        hospedagem.setModalidade(dto.modalidade());
        hospedagem.setDataEntrada(dto.dataEntrada());
        hospedagem.setDataSaida(dto.dataSaida());
        hospedagem.setObservacoes(dto.observacoes());
        if (dto.precoAcordado() != null) {
            hospedagem.setPrecoAcordado(dto.precoAcordado());
        }
        return hospedagem;
    }

    public HospedagemResponseDTO toResponseDTO(Hospedagem hospedagem) {
        return new HospedagemResponseDTO(
                hospedagem.getId(),
                hospedagem.getPet().getId(),
                hospedagem.getPet().getNome(),
                hospedagem.getPet().getTutor() != null ? hospedagem.getPet().getTutor().getNome() : null,
                hospedagem.getModalidade(),
                hospedagem.getDataEntrada(),
                hospedagem.getDataSaida(),
                hospedagem.getStatus(),
                hospedagem.getObservacoes(),
                hospedagem.getMotivoCancelamento(),
                hospedagem.getCriadoEm(),
                hospedagem.getServico() != null ? hospedagem.getServico().getId() : null,
                hospedagem.getServico() != null ? hospedagem.getServico().getNome() : null,
                hospedagem.getPrecoAcordado(),
                hospedagem.getCheckInEm(),
                hospedagem.getCheckOutEm()
        );
    }

    public void updateEntityFromDTO(HospedagemRequestDTO dto, Hospedagem hospedagem) {
        hospedagem.setModalidade(dto.modalidade());
        hospedagem.setDataEntrada(dto.dataEntrada());
        hospedagem.setDataSaida(dto.dataSaida());
        hospedagem.setObservacoes(dto.observacoes());
        if (dto.precoAcordado() != null) {
            hospedagem.setPrecoAcordado(dto.precoAcordado());
        }
    }
}