package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.UnidadeServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PacoteResponseDTO(
        Long id,
        String nome,
        String descricao,
        Long servicoId,
        String servicoNome,
        UnidadeServico unidade,
        Integer quantidade,
        BigDecimal precoTotal,
        Integer validadeDias,
        Long descontoId,
        String descontoCodigo,
        Boolean ativo,
        LocalDateTime criadoEm
) {}
