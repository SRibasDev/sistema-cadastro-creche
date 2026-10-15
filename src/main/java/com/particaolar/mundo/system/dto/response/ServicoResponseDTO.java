package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.TipoServico;
import com.particaolar.mundo.system.enums.UnidadeCobranca;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoResponseDTO(
        Long id,
        String nome,
        String descricao,
        TipoServico tipo,
        BigDecimal preco,
        UnidadeCobranca unidade,
        Boolean ativo,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {}
