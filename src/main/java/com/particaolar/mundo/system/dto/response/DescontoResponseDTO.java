package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.TipoDesconto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DescontoResponseDTO(
        Long id,
        String codigo,
        String nome,
        TipoDesconto tipo,
        BigDecimal valor,
        LocalDate validade,
        Boolean ativo,
        LocalDateTime criadoEm
) {}
