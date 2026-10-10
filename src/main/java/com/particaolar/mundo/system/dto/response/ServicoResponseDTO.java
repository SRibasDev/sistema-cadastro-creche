package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.UnidadeServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        UnidadeServico unidade,
        Boolean ativo,
        LocalDateTime criadoEm
) {}
