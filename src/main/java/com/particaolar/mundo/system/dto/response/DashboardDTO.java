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
        List<HospedagemResponseDTO> proximasChegadas,
        long vacinasVencidasQtd,
        long vacinasAVencerQtd,
        List<VacinacaoResponseDTO> vacinasVencidas,
        List<VacinacaoResponseDTO> vacinasAVencer
) {
    /** Construtor legado (sem alertas de vacina) — mantém compatibilidade com testes existentes. */
    public DashboardDTO(
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
            List<HospedagemResponseDTO> proximasChegadas) {
        this(petsPresentesAgora, entradasPrevistasHoje, saidasPrevistasHoje,
                ocupacaoCrecheHoje, capacidadeCreche, ocupacaoHotelHoje, capacidadeHotel,
                totalCobrancasPendentesValor, totalCobrancasPendentesQtd,
                presentesNoMomento, proximasChegadas, 0L, 0L, List.of(), List.of());
    }
}
