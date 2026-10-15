package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.dto.response.DashboardDTO;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import com.particaolar.mundo.system.dto.response.VacinacaoResponseDTO;
import com.particaolar.mundo.system.enums.Modalidade;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import com.particaolar.mundo.system.mapper.HospedagemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final HospedagemRepository hospedagemRepository;
    private final HospedagemMapper hospedagemMapper;
    private final CobrancaService cobrancaService;
    private final VacinacaoService vacinacaoService;

    @Value("${app.creche.capacidade.creche:10}")
    private int capacidadeCreche = 10;

    @Value("${app.creche.capacidade.hotel:20}")
    private int capacidadeHotel = 20;

    private static final Set<StatusHospedagem> ACTIVE_STATUSES =
            Set.of(StatusHospedagem.AGENDADA, StatusHospedagem.EM_ANDAMENTO);

    @Transactional(readOnly = true)
    public DashboardDTO obterMetricas() {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicioHoje = hoje.atStartOfDay();
        LocalDateTime fimHoje = hoje.plusDays(1).atStartOfDay();

        long petsPresentesAgora = hospedagemRepository.countByStatus(StatusHospedagem.EM_ANDAMENTO);
        long entradasPrevistasHoje = hospedagemRepository.countByStatusAndDataEntradaBetween(
                StatusHospedagem.AGENDADA, inicioHoje, fimHoje);
        long saidasPrevistasHoje = hospedagemRepository.countByStatusAndDataSaidaBetween(
                StatusHospedagem.EM_ANDAMENTO, inicioHoje, fimHoje);

        long ocupacaoCrecheHoje = hospedagemRepository.contarNoDia(
                Modalidade.CRECHE, ACTIVE_STATUSES, inicioHoje, fimHoje);
        long ocupacaoHotelHoje = hospedagemRepository.contarNoDia(
                Modalidade.HOTEL, ACTIVE_STATUSES, inicioHoje, fimHoje);

        BigDecimal pendenciasValor = cobrancaService.obterTotalSaldoDevedor();
        long pendenciasQtd = cobrancaService.obterQuantidadePendentes();

        List<HospedagemResponseDTO> presentes = hospedagemRepository.findByStatus(StatusHospedagem.EM_ANDAMENTO)
                .stream().map(hospedagemMapper::toResponseDTO).toList();

        List<HospedagemResponseDTO> proximas = hospedagemRepository
                .findTop10ByStatusAndDataEntradaAfterOrderByDataEntradaAsc(
                        StatusHospedagem.AGENDADA, LocalDateTime.now().minusHours(2)
                ).stream().map(hospedagemMapper::toResponseDTO).toList();

        List<VacinacaoResponseDTO> vacinasVencidas = vacinacaoService.listarVencidas();
        List<VacinacaoResponseDTO> vacinasAVencer = vacinacaoService.listarAVencer();

        return new DashboardDTO(
                petsPresentesAgora,
                entradasPrevistasHoje,
                saidasPrevistasHoje,
                ocupacaoCrecheHoje,
                capacidadeCreche,
                ocupacaoHotelHoje,
                capacidadeHotel,
                pendenciasValor,
                pendenciasQtd,
                presentes,
                proximas,
                vacinasVencidas.size(),
                vacinasAVencer.size(),
                vacinasVencidas.stream().limit(10).toList(),
                vacinasAVencer.stream().limit(10).toList()
        );
    }
}
