package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.OrigemCobranca;
import com.particaolar.mundo.system.enums.StatusCobranca;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CobrancaResponseDTO(
        Long id,
        OrigemCobranca origem,
        Long reservaId,
        Long pacoteCompradoId,
        Long petId,
        String petNome,
        Long tutorId,
        String tutorNome,
        String descricao,
        BigDecimal valor,
        Long descontoId,
        String descontoCodigo,
        BigDecimal valorFinal,
        LocalDate dataVencimento,
        StatusCobranca status,
        LocalDate dataPagamento,
        String observacoes,
        LocalDateTime criadoEm
) {}
