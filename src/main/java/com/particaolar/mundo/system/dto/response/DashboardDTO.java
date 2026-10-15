package com.particaolar.mundo.system.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardDTO(
        long petsPresentesAgora,
        long entradasPrevistasHoje,
        long saidasPrevistasHoje,
        long ocupacaoCrecheHoje,
        int capacidadeCreche,
        long ocupacaoHotelHoje,
        int capacidadeHotel,
        BigDecimal totalCobrancasPendentesValor,
        long totalCobrancasPendentesQtd,
        List<HospedagemResponseDTO> presentesNoMomento,
        List<HospedagemResponseDTO> proximasChegadas
) {}
