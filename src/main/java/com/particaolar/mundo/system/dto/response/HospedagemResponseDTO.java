package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.StatusHospedagem;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record HospedagemResponseDTO(

        Long id,
        Long petId,
        String petNome,
        String tutorNome,
        LocalDate dataEntrada,
        LocalDate dataSaida,
        StatusHospedagem status,
        String observacoes,
        LocalDateTime criadoEm
) {}
