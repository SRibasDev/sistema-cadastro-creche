package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.dto.request.ReservaRequestDTO;
import com.particaolar.mundo.system.dto.response.ReservaResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ReservaMapper {

    public Reserva toEntity(ReservaRequestDTO dto) {
        Reserva reserva = new Reserva();
        reserva.setTipo(dto.tipo());
        reserva.setDataEntrada(dto.dataEntrada());
        reserva.setDataSaida(dto.dataSaida());
        reserva.setHoraEntrada(dto.horaEntrada());
        reserva.setHoraSaida(dto.horaSaida());
        reserva.setObservacoes(dto.observacoes());
        return reserva;
    }

    public ReservaResponseDTO toResponseDTO(Reserva reserva) {
        return new ReservaResponseDTO(
                reserva.getId(),
                reserva.getPet().getId(),
                reserva.getPet().getNome(),
                reserva.getPet().getTutor() != null ? reserva.getPet().getTutor().getId() : null,
                reserva.getPet().getTutor() != null ? reserva.getPet().getTutor().getNome() : null,
                reserva.getTipo(),
                reserva.getDataEntrada(),
                reserva.getDataSaida(),
                reserva.getHoraEntrada(),
                reserva.getHoraSaida(),
                reserva.getStatus(),
                reserva.getObservacoes(),
                reserva.getMotivoCancelamento(),
                reserva.getCriadoEm()
        );
    }

    public void updateEntityFromDTO(ReservaRequestDTO dto, Reserva reserva) {
        reserva.setTipo(dto.tipo());
        reserva.setDataEntrada(dto.dataEntrada());
        reserva.setDataSaida(dto.dataSaida());
        reserva.setHoraEntrada(dto.horaEntrada());
        reserva.setHoraSaida(dto.horaSaida());
        reserva.setObservacoes(dto.observacoes());
    }
}
