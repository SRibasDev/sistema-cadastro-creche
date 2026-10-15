package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.Modalidade;
import com.particaolar.mundo.system.enums.StatusHospedagem;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HospedagemResponseDTO(
        Long id,
        Long petId,
        String petNome,
        String tutorNome,
        Modalidade modalidade,
        LocalDateTime dataEntrada,
        LocalDateTime dataSaida,
        StatusHospedagem status,
        String observacoes,
        String motivoCancelamento,
        LocalDateTime criadoEm,
        Long servicoId,
        String servicoNome,
        BigDecimal precoAcordado,
        LocalDateTime checkInEm,
        LocalDateTime checkOutEm
) {
    public HospedagemResponseDTO(
            Long id,
            Long petId,
            String petNome,
            String tutorNome,
            Modalidade modalidade,
            LocalDateTime dataEntrada,
            LocalDateTime dataSaida,
            StatusHospedagem status,
            String observacoes,
            String motivoCancelamento,
            LocalDateTime criadoEm
    ) {
        this(id, petId, petNome, tutorNome, modalidade, dataEntrada, dataSaida, status, observacoes, motivoCancelamento, criadoEm, null, null, null, null, null);
    }
}
