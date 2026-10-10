package com.particaolar.mundo.system.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservaHistoricoResponseDTO(

        Long id,
        Long reservaId,
        String tipoEvento,
        LocalDate dataAnterior,
        LocalDate dataSaidaAnterior,
        LocalDate dataNova,
        LocalDate dataSaidaNova,
        String statusAnterior,
        String statusNovo,
        String motivo,
        String usuario,
        LocalDateTime criadoEm
) {}