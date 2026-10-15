package com.particaolar.mundo.system.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CobrancaRequestDTO(
        @NotNull(message = "O ID do tutor é obrigatório")
        Long tutorId,

        Long hospedagemId,

        @NotBlank(message = "A descrição da cobrança é obrigatória")
        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "O valor total é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor total deve ser positivo")
        BigDecimal valorTotal,

        @DecimalMin(value = "0.00", message = "O desconto não pode ser negativo")
        BigDecimal desconto,

        @NotNull(message = "A data de vencimento é obrigatória")
        LocalDate dataVencimento,

        @Size(max = 500, message = "Observações deve ter no máximo 500 caracteres")
        String observacoes
) {}
