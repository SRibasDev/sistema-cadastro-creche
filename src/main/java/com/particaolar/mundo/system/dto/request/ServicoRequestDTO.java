package com.particaolar.mundo.system.dto.request;

import com.particaolar.mundo.system.enums.UnidadeServico;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicoRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 80, message = "Nome deve ter no máximo 80 caracteres")
        String nome,

        @Size(max = 200, message = "Descrição deve ter no máximo 200 caracteres")
        String descricao,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.00", inclusive = false, message = "Preço deve ser maior que zero")
        BigDecimal preco,

        @NotNull(message = "Unidade é obrigatória")
        UnidadeServico unidade
) {}
