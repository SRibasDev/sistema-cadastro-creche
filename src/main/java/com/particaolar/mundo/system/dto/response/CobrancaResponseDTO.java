package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.StatusCobranca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record CobrancaResponseDTO(
        Long id,
        Long tutorId,
        String tutorNome,
        Long hospedagemId,
        String descricao,
        BigDecimal valorTotal,
        BigDecimal desconto,
        BigDecimal valorCobrado,
        BigDecimal valorPago,
        BigDecimal saldoDevedor,
        LocalDate dataVencimento,
        LocalDateTime dataEmissao,
        StatusCobranca status,
        String observacoes,
        List<PagamentoResponseDTO> pagamentos
) {}
