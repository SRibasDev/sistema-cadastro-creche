package com.particaolar.mundo.system.dto.response;

import com.particaolar.mundo.system.enums.TipoPertence;

import java.time.LocalDateTime;

public record PetPertenceResponseDTO(

        Long id,
        Long petId,
        TipoPertence tipo,
        String descricao,
        String quantidade,
        Integer quantidadeNumero,
        String observacoes,
        Boolean ativo,
        LocalDateTime criadoEm
) {}
