package com.particaolar.mundo.system.dto.request;

import com.particaolar.mundo.system.enums.TipoServico;
import com.particaolar.mundo.system.enums.UnidadeCobranca;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicoRequestDTO(
        @NotBlank(message = "O nome do serviço é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        String descricao,

        @NotNull(message = "O tipo do serviço é obrigatório")
        TipoServico tipo,

        @NotNull(message = "O preço é obrigatório")
        @DecimalMin(value = "0.00", message = "O preço não pode ser negativo")
        BigDecimal preco,

        @NotNull(message = "A unidade de cobrança é obrigatória")
        UnidadeCobranca unidade
) {}
