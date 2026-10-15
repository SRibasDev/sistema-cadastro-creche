package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.FormaPagamento;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PagamentoResponseDTO(
        Long id,
        Long cobrancaId,
        BigDecimal valor,
        LocalDateTime dataPagamento,
        FormaPagamento formaPagamento,
        String comprovante,
        String observacoes,
        String registradoPor
) {}
