package com.particaolar.mundo.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record VacinacaoRequestDTO(

        @NotBlank(message = "O nome da vacina é obrigatório")
        @Size(max = 100, message = "O nome da vacina deve ter no máximo 100 caracteres")
        String nomeVacina,

        @NotNull(message = "A data de aplicação é obrigatória")
        LocalDate dataAplicacao,

        @NotNull(message = "A data de vencimento é obrigatória")
        LocalDate dataVencimento,

        @Size(max = 50, message = "O lote deve ter no máximo 50 caracteres")
        String lote,

        @Size(max = 100, message = "O nome do veterinário deve ter no máximo 100 caracteres")
        String veterinario,

        @Size(max = 500, message = "As observações devem ter no máximo 500 caracteres")
        String observacoes
) {}
