package com.particaolar.mundo.system.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record HospedagemRequestDTO(

        @NotNull(message = "O ID do pet é obrigatório")
        Long petId,

        @NotNull(message = "A data de entrada é obrigatória")
        @FutureOrPresent(message = "A data de entrada não pode ser no passado")
        LocalDate dataEntrada,

        @NotNull(message = "A data de saída é obrigatória")
        @Future(message = "A data de saída deve ser no futuro")
        LocalDate dataSaida,

        @Size(max = 200, message = "Observações deve ter no máximo 200 caracteres")
        String observacoes
) {}