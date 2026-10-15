package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Cobranca;
import com.particaolar.mundo.system.domain.entity.Pagamento;
import com.particaolar.mundo.system.domain.repository.CobrancaRepository;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.repository.PagamentoRepository;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import com.particaolar.mundo.system.dto.response.RelatorioFinanceiroDTO;
import com.particaolar.mundo.system.mapper.HospedagemMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final CobrancaRepository cobrancaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final HospedagemRepository hospedagemRepository;
    private final HospedagemMapper hospedagemMapper;

    @Transactional(readOnly = true)
    public RelatorioFinanceiroDTO gerarRelatorioFinanceiro(LocalDate inicio, LocalDate fim) {
        if (inicio == null) inicio = LocalDate.now().minusDays(30);
        if (fim == null) fim = LocalDate.now();
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final deve ser igual ou posterior à data inicial.");
        }

        LocalDateTime inicioDT = inicio.atStartOfDay();
        LocalDateTime fimDT = fim.plusDays(1).atStartOfDay();

        List<Cobranca> cobrancas = cobrancaRepository.buscarPorPeriodoEmissao(inicioDT, fimDT);
        List<Pagamento> pagamentos = pagamentoRepository.findByDataPagamentoBetween(inicioDT, fimDT);

        BigDecimal totalEmitido = cobrancas.stream()
                .map(Cobranca::getValorCobrado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalRecebido = pagamentos.stream()
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPendente = totalEmitido.subtract(totalRecebido);
        if (totalPendente.compareTo(BigDecimal.ZERO) < 0) {
            totalPendente = BigDecimal.ZERO;
        }

        return new RelatorioFinanceiroDTO(
                inicio,
                fim,
                totalEmitido,
                totalRecebido,
                totalPendente,
                cobrancas.size(),
                pagamentos.size()
        );
    }

    @Transactional(readOnly = true)
    public List<HospedagemResponseDTO> gerarRelatorioReservas(LocalDate inicio, LocalDate fim) {
        if (inicio == null) inicio = LocalDate.now().minusDays(7);
        if (fim == null) fim = LocalDate.now().plusDays(7);
        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final deve ser igual ou posterior à data inicial.");
        }

        LocalDateTime inicioDT = inicio.atStartOfDay();
        LocalDateTime fimDT = fim.plusDays(1).atStartOfDay();

        return hospedagemRepository.findByDataEntradaBetween(inicioDT, fimDT).stream()
                .map(hospedagemMapper::toResponseDTO)
                .toList();
    }
}
