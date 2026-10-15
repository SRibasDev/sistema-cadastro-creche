package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.service.CobrancaService;
import com.particaolar.mundo.system.domain.service.DashboardService;
import com.particaolar.mundo.system.dto.response.DashboardDTO;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import com.particaolar.mundo.system.enums.Modalidade;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import com.particaolar.mundo.system.mapper.HospedagemMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private HospedagemRepository hospedagemRepository;

    @Mock
    private HospedagemMapper hospedagemMapper;

    @Mock
    private CobrancaService cobrancaService;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    @DisplayName("Deve compilar métricas em tempo real para o dashboard operacional")
    void deveObterMetricasComSucesso() {
        when(hospedagemRepository.countByStatus(StatusHospedagem.EM_ANDAMENTO)).thenReturn(3L);
        when(hospedagemRepository.countByStatusAndDataEntradaBetween(eq(StatusHospedagem.AGENDADA), any(), any()))
                .thenReturn(5L);
        when(hospedagemRepository.countByStatusAndDataSaidaBetween(eq(StatusHospedagem.EM_ANDAMENTO), any(), any()))
                .thenReturn(2L);
        when(hospedagemRepository.contarNoDia(eq(Modalidade.CRECHE), any(), any(), any())).thenReturn(4L);
        when(hospedagemRepository.contarNoDia(eq(Modalidade.HOTEL), any(), any(), any())).thenReturn(6L);
        when(cobrancaService.obterTotalSaldoDevedor()).thenReturn(new BigDecimal("450.00"));
        when(cobrancaService.obterQuantidadePendentes()).thenReturn(3L);

        Hospedagem h = new Hospedagem();
        h.setId(1L);
        HospedagemResponseDTO respDTO = new HospedagemResponseDTO(
                1L, 1L, "Rex", "Carlos", Modalidade.CRECHE,
                LocalDateTime.now(), LocalDateTime.now().plusHours(8),
                StatusHospedagem.EM_ANDAMENTO, null, null, LocalDateTime.now()
        );
        when(hospedagemRepository.findByStatus(StatusHospedagem.EM_ANDAMENTO)).thenReturn(List.of(h));
        when(hospedagemMapper.toResponseDTO(h)).thenReturn(respDTO);

        DashboardDTO dto = dashboardService.obterMetricas();

        assertNotNull(dto);
        assertEquals(3L, dto.petsPresentesAgora());
        assertEquals(5L, dto.entradasPrevistasHoje());
        assertEquals(2L, dto.saidasPrevistasHoje());
        assertEquals(4L, dto.ocupacaoCrecheHoje());
        assertEquals(6L, dto.ocupacaoHotelHoje());
        assertEquals(new BigDecimal("450.00"), dto.totalCobrancasPendentesValor());
        assertEquals(3L, dto.totalCobrancasPendentesQtd());
        assertEquals(1, dto.presentesNoMomento().size());
    }
}
