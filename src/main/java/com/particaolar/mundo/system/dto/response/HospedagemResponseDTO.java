package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.Modalidade;
import com.particaolar.mundo.system.enums.StatusHospedagem;
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
        LocalDateTime criadoEm
) {}
