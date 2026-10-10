package com.particaolar.mundo.system.dto.request;

import com.particaolar.mundo.system.enums.OrigemCobranca;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CobrancaRequestDTO(

        @NotNull(message = "Origem é obrigatória")
        OrigemCobranca origem,

        /** Obrigatório quando origem = RESERVA. */
        Long reservaId,

        /** Obrigatório quando origem = PACOTE. */
        Long pacoteCompradoId,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
        String descricao,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.00", inclusive = false, message = "Valor deve ser maior que zero")
        BigDecimal valor,

        Long descontoId,

        @NotNull(message = "Data de vencimento é obrigatória")
        LocalDate dataVencimento,

        @Size(max = 200, message = "Observações deve ter no máximo 200 caracteres")
        String observacoes
) {}
