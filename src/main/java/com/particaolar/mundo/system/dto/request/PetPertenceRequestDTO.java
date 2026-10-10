package com.particaolar.mundo.system.dto.request;

import com.particaolar.mundo.system.enums.TipoPertence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record PetPertenceRequestDTO(

        @NotNull(message = "O tipo do pertence é obrigatório")
        TipoPertence tipo,

        @NotBlank(message = "Descrição é obrigatória")
        @Size(max = 150, message = "Descrição deve ter no máximo 150 caracteres")
        String descricao,

        @Size(max = 50, message = "Quantidade deve ter no máximo 50 caracteres")
        String quantidade,

        @Positive(message = "Quantidade numérica deve ser positiva")
        Integer quantidadeNumero,

        @Size(max = 200, message = "Observações deve ter no máximo 200 caracteres")
        String observacoes
) {}
