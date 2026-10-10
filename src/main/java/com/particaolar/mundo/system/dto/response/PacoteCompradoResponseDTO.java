package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.UnidadeServico;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PacoteCompradoResponseDTO(
        Long id,
        Long pacoteId,
        String pacoteNome,
        UnidadeServico unidade,
        Long petId,
        String petNome,
        Integer saldo,
        BigDecimal valorPago,
        LocalDate validade,
        Boolean ativo,
        LocalDateTime criadoEm
) {}
