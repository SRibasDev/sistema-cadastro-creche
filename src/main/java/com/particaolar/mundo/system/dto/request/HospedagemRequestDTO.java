package com.particaolar.mundo.system.dto.request;

import com.particaolar.mundo.system.enums.Modalidade;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record HospedagemRequestDTO(

        @NotNull(message = "O ID do pet é obrigatório")
        Long petId,

        @NotNull(message = "A modalidade é obrigatória")
        Modalidade modalidade,

        @NotNull(message = "A data/hora de entrada é obrigatória")
        @FutureOrPresent(message = "A entrada não pode ser no passado")
        LocalDateTime dataEntrada,

        @NotNull(message = "A data/hora de saída é obrigatória")
        @Future(message = "A saída deve ser no futuro")
        LocalDateTime dataSaida,

        @Size(max = 200, message = "Observações deve ter no máximo 200 caracteres")
        String observacoes,

        Long servicoId,

        @DecimalMin(value = "0.00", message = "O preço acordado não pode ser negativo")
        BigDecimal precoAcordado
) {
    public HospedagemRequestDTO(Long petId, Modalidade modalidade, LocalDateTime dataEntrada, LocalDateTime dataSaida, String observacoes) {
        this(petId, modalidade, dataEntrada, dataSaida, observacoes, null, null);
    }
}