package com.particaolar.mundo.system.dto.request;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/** Corpo do reagendamento de uma reserva existente. */
public record ReagendarRequestDTO(

        @Size(max = 200, message = "Motivo deve ter no máximo 200 caracteres")
        String motivo,

        LocalDate dataEntrada,

        LocalDate dataSaida,

        LocalTime horaEntrada,

        LocalTime horaSaida
) {}