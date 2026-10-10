package com.particaolar.mundo.system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Compra de um pacote por um pet (gera saldo). */
public record PacoteCompraRequestDTO(

        @NotNull(message = "O pet é obrigatório")
        Long petId,

        /** Valor efetivamente pago (pode divergir do preçoTotal com desconto na venda). */
        @Positive(message = "Valor pago deve ser positivo")
        BigDecimal valorPago
) {}
