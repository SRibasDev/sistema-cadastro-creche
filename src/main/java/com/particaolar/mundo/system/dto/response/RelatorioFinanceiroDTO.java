package com.particaolar.mundo.system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RelatorioFinanceiroDTO(
        LocalDate periodoInicio,
        LocalDate periodoFim,
        BigDecimal totalEmitido,
        BigDecimal totalRecebido,
        BigDecimal totalPendente,
        long quantidadeCobrancas,
        long quantidadePagamentos
) {}
