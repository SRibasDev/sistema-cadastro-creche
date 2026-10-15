package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Cobranca;
import com.particaolar.mundo.system.domain.entity.Pagamento;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.CobrancaRepository;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.repository.PagamentoRepository;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.domain.service.AuditLogService;
import com.particaolar.mundo.system.domain.service.CobrancaService;
import com.particaolar.mundo.system.dto.request.CobrancaRequestDTO;
import com.particaolar.mundo.system.dto.request.PagamentoRequestDTO;
import com.particaolar.mundo.system.dto.response.CobrancaResponseDTO;
import com.particaolar.mundo.system.dto.response.PagamentoResponseDTO;
import com.particaolar.mundo.system.enums.FormaPagamento;
import com.particaolar.mundo.system.enums.StatusCobranca;
import com.particaolar.mundo.system.exception.CobrancaNotFoundException;
import com.particaolar.mundo.system.mapper.CobrancaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CobrancaServiceTest {

    @Mock
    private CobrancaRepository cobrancaRepository;

    @Mock
    private PagamentoRepository pagamentoRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private HospedagemRepository hospedagemRepository;

    @Mock
    private CobrancaMapper cobrancaMapper;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private CobrancaService cobrancaService;

    private Tutor tutor;
    private Cobranca cobranca;
    private CobrancaRequestDTO cobrancaRequestDTO;
    private CobrancaResponseDTO cobrancaResponseDTO;

    @BeforeEach
    void setUp() {
        tutor = new Tutor();
        tutor.setId(1L);
        tutor.setNome("João Silva");

        cobranca = new Cobranca();
        cobranca.setId(10L);
        cobranca.setTutor(tutor);
        cobranca.setDescricao("Hospedagem 2 diárias");
        cobranca.setValorTotal(new BigDecimal("200.00"));
        cobranca.setDesconto(new BigDecimal("20.00"));
        cobranca.setValorCobrado(new BigDecimal("180.00"));
        cobranca.setValorPago(BigDecimal.ZERO);
        cobranca.setDataVencimento(LocalDate.now().plusDays(5));
        cobranca.setDataEmissao(LocalDateTime.now());
        cobranca.setStatus(StatusCobranca.PENDENTE);

        cobrancaRequestDTO = new CobrancaRequestDTO(
                1L, null, "Hospedagem 2 diárias",
                new BigDecimal("200.00"), new BigDecimal("20.00"),
                LocalDate.now().plusDays(5), "Desconto acordado"
        );

        cobrancaResponseDTO = new CobrancaResponseDTO(
                10L, 1L, "João Silva", null, "Hospedagem 2 diárias",
                new BigDecimal("200.00"), new BigDecimal("20.00"), new BigDecimal("180.00"),
                BigDecimal.ZERO, new BigDecimal("180.00"), LocalDate.now().plusDays(5),
                LocalDateTime.now(), StatusCobranca.PENDENTE, "Desconto acordado", null
        );
    }

    @Test
    @DisplayName("Deve criar cobrança com sucesso calculando valor cobrado com desconto")
    void deveCriarCobrancaComSucesso() {
        when(tutorRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(tutor));
        when(cobrancaMapper.toEntity(cobrancaRequestDTO)).thenReturn(cobranca);
        when(cobrancaRepository.save(any(Cobranca.class))).thenReturn(cobranca);
        when(cobrancaMapper.toResponseDTO(cobranca)).thenReturn(cobrancaResponseDTO);

        CobrancaResponseDTO res = cobrancaService.criar(cobrancaRequestDTO, "admin@creche.com");

        assertNotNull(res);
        assertEquals(new BigDecimal("180.00"), res.valorCobrado());
        verify(cobrancaRepository).save(any(Cobranca.class));
    }

    @Test
    @DisplayName("Deve impedir desconto maior que valor total")
    void deveImpedirDescontoMaiorQueValorTotal() {
        when(tutorRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(tutor));
        CobrancaRequestDTO dtoInvalido = new CobrancaRequestDTO(
                1L, null, "Serviço",
                new BigDecimal("100.00"), new BigDecimal("150.00"),
                LocalDate.now().plusDays(5), null
        );

        assertThrows(IllegalArgumentException.class, () ->
                cobrancaService.criar(dtoInvalido, "admin@creche.com"));
    }

    @Test
    @DisplayName("Deve registrar pagamento parcial com sucesso e atualizar status para PAGA_PARCIAL")
    void deveRegistrarPagamentoParcial() {
        when(cobrancaRepository.findById(10L)).thenReturn(Optional.of(cobranca));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(i -> i.getArgument(0));

        PagamentoResponseDTO pagDTO = new PagamentoResponseDTO(
                1L, 10L, new BigDecimal("100.00"), LocalDateTime.now(),
                FormaPagamento.PIX, "PIX123", null, "func@creche.com"
        );
        when(cobrancaMapper.toPagamentoResponseDTO(any(Pagamento.class))).thenReturn(pagDTO);

        PagamentoRequestDTO req = new PagamentoRequestDTO(
                10L, new BigDecimal("100.00"), FormaPagamento.PIX, "PIX123", null
        );

        PagamentoResponseDTO res = cobrancaService.registrarPagamento(req, "func@creche.com");

        assertNotNull(res);
        assertEquals(StatusCobranca.PAGA_PARCIAL, cobranca.getStatus());
        assertEquals(new BigDecimal("100.00"), cobranca.getValorPago());
    }

    @Test
    @DisplayName("Deve registrar pagamento integral e atualizar status para PAGA")
    void deveRegistrarPagamentoIntegral() {
        when(cobrancaRepository.findById(10L)).thenReturn(Optional.of(cobranca));
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(i -> i.getArgument(0));

        PagamentoResponseDTO pagDTO = new PagamentoResponseDTO(
                1L, 10L, new BigDecimal("180.00"), LocalDateTime.now(),
                FormaPagamento.PIX, "PIX999", null, "func@creche.com"
        );
        when(cobrancaMapper.toPagamentoResponseDTO(any(Pagamento.class))).thenReturn(pagDTO);

        PagamentoRequestDTO req = new PagamentoRequestDTO(
                10L, new BigDecimal("180.00"), FormaPagamento.PIX, "PIX999", null
        );

        cobrancaService.registrarPagamento(req, "func@creche.com");

        assertEquals(StatusCobranca.PAGA, cobranca.getStatus());
        assertEquals(new BigDecimal("180.00"), cobranca.getValorPago());
    }

    @Test
    @DisplayName("Deve impedir pagamento que excede o saldo devedor")
    void deveImpedirPagamentoMaiorQueSaldoDevedor() {
        when(cobrancaRepository.findById(10L)).thenReturn(Optional.of(cobranca));

        PagamentoRequestDTO req = new PagamentoRequestDTO(
                10L, new BigDecimal("200.00"), FormaPagamento.DINHEIRO, null, null
        );

        assertThrows(IllegalArgumentException.class, () ->
                cobrancaService.registrarPagamento(req, "func@creche.com"));
    }

    @Test
    @DisplayName("Deve impedir pagamento em cobrança já quitada")
    void deveImpedirPagamentoEmCobrancaQuitada() {
        cobranca.setStatus(StatusCobranca.PAGA);
        when(cobrancaRepository.findById(10L)).thenReturn(Optional.of(cobranca));

        PagamentoRequestDTO req = new PagamentoRequestDTO(
                10L, new BigDecimal("50.00"), FormaPagamento.DINHEIRO, null, null
        );

        assertThrows(IllegalStateException.class, () ->
                cobrancaService.registrarPagamento(req, "func@creche.com"));
    }

    @Test
    @DisplayName("Deve cancelar cobrança sem pagamentos")
    void deveCancelarCobrancaSemPagamentos() {
        when(cobrancaRepository.findById(10L)).thenReturn(Optional.of(cobranca));

        cobrancaService.cancelar(10L, "Cliente solicitou cancelamento", "admin@creche.com");

        assertEquals(StatusCobranca.CANCELADA, cobranca.getStatus());
        verify(cobrancaRepository).save(cobranca);
    }

    @Test
    @DisplayName("Deve impedir cancelamento de cobrança com pagamentos já realizados")
    void deveImpedirCancelarCobrancaComPagamento() {
        cobranca.setValorPago(new BigDecimal("50.00"));
        when(cobrancaRepository.findById(10L)).thenReturn(Optional.of(cobranca));

        assertThrows(IllegalStateException.class, () ->
                cobrancaService.cancelar(10L, "Motivo", "admin@creche.com"));
    }
}
