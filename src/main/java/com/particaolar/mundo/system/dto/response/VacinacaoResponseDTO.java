package com.particaolar.mundo.system.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record VacinacaoResponseDTO(
        Long id,
        Long petId,
        String petNome,
        String nomeVacina,
        LocalDate dataAplicacao,
        LocalDate dataVencimento,
        String lote,
        String veterinario,
        String observacoes,
        String situacao,
        Boolean ativo,
        LocalDateTime criadoEm
) {}
