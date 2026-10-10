package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.StatusReserva;
import com.particaolar.mundo.system.enums.TipoReserva;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record ReservaResponseDTO(

        Long id,
        Long petId,
        String petNome,
        Long tutorId,
        String tutorNome,
        TipoReserva tipo,
        LocalDate dataEntrada,
        LocalDate dataSaida,
        LocalTime horaEntrada,
        LocalTime horaSaida,
        StatusReserva status,
        String observacoes,
        String motivoCancelamento,
        LocalDateTime criadoEm
) {}