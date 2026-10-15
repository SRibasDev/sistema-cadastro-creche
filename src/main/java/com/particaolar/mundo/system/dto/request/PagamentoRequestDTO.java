package com.particaolar.mundo.system.dto.request;

import com.particaolar.mundo.system.enums.FormaPagamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record PagamentoRequestDTO(
        @NotNull(message = "O ID da cobrança é obrigatório")
        Long cobrancaId,

        @NotNull(message = "O valor do pagamento é obrigatório")
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valor,

        @NotNull(message = "A forma de pagamento é obrigatória")
        FormaPagamento formaPagamento,

        @Size(max = 100, message = "O comprovante/código deve ter no máximo 100 caracteres")
        String comprovante,

        @Size(max = 500, message = "Observações deve ter no máximo 500 caracteres")
        String observacoes
) {}
