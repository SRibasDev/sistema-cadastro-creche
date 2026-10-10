package com.particaolar.mundo.system.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PacoteRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 80, message = "Nome deve ter no máximo 80 caracteres")
        String nome,

        @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
        String descricao,

        @NotNull(message = "Serviço é obrigatório")
        Long servicoId,

        @NotNull(message = "Quantidade é obrigatória")
        @Min(value = 1, message = "Quantidade deve ser no mínimo 1")
        Integer quantidade,

        @NotNull(message = "Preço total é obrigatório")
        @DecimalMin(value = "0.00", inclusive = false, message = "Preço total deve ser maior que zero")
        BigDecimal precoTotal,

        @NotNull(message = "Validade em dias é obrigatória")
        @Min(value = 1, message = "Validade em dias deve ser no mínimo 1")
        Integer validadeDias,

        Long descontoId
) {}
