package com.particaolar.mundo.system.dto.request;

import com.particaolar.mundo.system.enums.TipoReserva;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record ReservaRequestDTO(

        @NotNull(message = "O ID do pet é obrigatório")
        Long petId,

        @NotNull(message = "O tipo da reserva é obrigatório")
        TipoReserva tipo,

        @NotNull(message = "A data de entrada é obrigatória")
        LocalDate dataEntrada,

        @NotNull(message = "A data de saída é obrigatória")
        LocalDate dataSaida,

        LocalTime horaEntrada,

        LocalTime horaSaida,

        @Size(max = 200, message = "Observações deve ter no máximo 200 caracteres")
        String observacoes
) {}