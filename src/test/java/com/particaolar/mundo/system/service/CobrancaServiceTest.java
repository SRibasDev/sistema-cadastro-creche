package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Cobranca;
import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.CobrancaRepository;
import com.particaolar.mundo.system.domain.repository.DescontoRepository;
import com.particaolar.mundo.system.domain.repository.PacoteCompradoRepository;
import com.particaolar.mundo.system.domain.repository.ReservaRepository;
import com.particaolar.mundo.system.domain.service.CobrancaService;
import com.particaolar.mundo.system.dto.request.CobrancaRequestDTO;
import com.particaolar.mundo.system.dto.response.CobrancaResponseDTO;
import com.particaolar.mundo.system.enums.OrigemCobranca;
import com.particaolar.mundo.system.enums.StatusCobranca;
import com.particaolar.mundo.system.exception.CobrancaNotFoundException;
import com.particaolar.mundo.system.mapper.CobrancaMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CobrancaServiceTest {

    @Mock private CobrancaRepository cobrancaRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private PacoteCompradoRepository pacoteCompradoRepository;
    @Mock private DescontoRepository descontoRepository;
    @Mock private CobrancaMapper cobrancaMapper;

    @InjectMocks
    private CobrancaService cobrancaService;

    private final Clock clock = Clock.fixed(
            ZonedDateTime.of(2026, 6, 10, 8, 0, 0, 0, ZoneId.of("America/Sao_Paulo")).toInstant(),
            ZoneId.of("America/Sao_Paulo"));

    @BeforeEach
    void setUp() throws Exception {
        Field f = CobrancaService.class.getDeclaredField("clock");
        f.setAccessible(true);
        f.set(cobrancaService, clock);
    }

    private Reserva reservaValida() {
        Tutor tutor = new Tutor();
        tutor.setId(1L);
        tutor.setNome("João");
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Rex");
        pet.setTutor(tutor);
        Reserva reserva = new Reserva();
        reserva.setId(1L);
        reserva.setPet(pet);
        return reserva;
    }

    private CobrancaRequestDTO dto() {
        return new CobrancaRequestDTO(OrigemCobranca.RESERVA, 1L, null,
                "Creche dia", new BigDecimal("80.00"), null,
                LocalDate.of(2026, 6, 20), null);
    }

    private CobrancaResponseDTO resposta() {
        return new CobrancaResponseDTO(1L, OrigemCobranca.RESERVA, 1L, null, 1L, "Rex",
                1L, "João", "Creche dia", new BigDecimal("80.00"), null, null,
                new BigDecimal("80.00"), LocalDate.of(2026, 6, 20),
                StatusCobranca.PENDENTE, null, null, java.time.LocalDateTime.now());
    }
    @Test
    void deveGerarCobrancaPorReserva() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaValida()));
        when(cobrancaRepository.save(any(Cobranca.class))).thenAnswer(inv -> inv.getArgument(0));
        when(cobrancaMapper.toResponseDTO(any())).thenReturn(resposta());

        CobrancaResponseDTO resultado = cobrancaService.salvar(dto());

        assertNotNull(resultado);
        assertEquals(StatusCobranca.PENDENTE, resultado.status());
        assertEquals(0, new BigDecimal("80.00").compareTo(resultado.valorFinal()));
        verify(cobrancaRepository).save(any(Cobranca.class));
    }

    @Test
    void deveRegistrarPagamento() {
        Cobranca cobranca = new Cobranca();
        cobranca.setId(1L);
        cobranca.setStatus(StatusCobranca.PENDENTE);

        when(cobrancaRepository.findById(1L)).thenReturn(Optional.of(cobranca));
        when(cobrancaMapper.toResponseDTO(cobranca)).thenReturn(new CobrancaResponseDTO(
                1L, OrigemCobranca.RESERVA, 1L, null, 1L, "Rex", 1L, "João", "Creche dia",
                new BigDecimal("80.00"), null, null, new BigDecimal("80.00"),
                LocalDate.of(2026, 6, 20), StatusCobranca.PAGA, LocalDate.of(2026, 6, 15),
                null, java.time.LocalDateTime.now()));

        CobrancaResponseDTO resultado = cobrancaService.registrarPagamento(1L, LocalDate.of(2026, 6, 15));

        assertEquals(StatusCobranca.PAGA, resultado.status());
        assertEquals(LocalDate.of(2026, 6, 15), resultado.dataPagamento());
    }

    @Test
    void naoDevePagarCobrancaJaPaga() {
        Cobranca cobranca = new Cobranca();
        cobranca.setId(1L);
        cobranca.setStatus(StatusCobranca.PAGA);

        when(cobrancaRepository.findById(1L)).thenReturn(Optional.of(cobranca));

        assertThrows(IllegalStateException.class,
                () -> cobrancaService.registrarPagamento(1L, null));
    }

    @Test
    void deveMarcarAtrasadas() {
        Cobranca vencida = new Cobranca();
        vencida.setId(1L);
        vencida.setStatus(StatusCobranca.PENDENTE);
        vencida.setDataVencimento(LocalDate.of(2026, 6, 1)); // antes de hoje (2026-06-10)

        when(cobrancaRepository.findByStatusAndDataVencimentoLessThan(
                eq(StatusCobranca.PENDENTE), any(LocalDate.class)))
                .thenReturn(List.of(vencida));

        int atualizadas = cobrancaService.atualizarAtrasadas();

        assertEquals(1, atualizadas);
        assertEquals(StatusCobranca.ATRASADA, vencida.getStatus());
    }

    @Test
    void naoDeveCancelarCobrancaPaga() {
        Cobranca cobranca = new Cobranca();
        cobranca.setId(1L);
        cobranca.setStatus(StatusCobranca.PAGA);

        when(cobrancaRepository.findById(1L)).thenReturn(Optional.of(cobranca));

        assertThrows(IllegalStateException.class,
                () -> cobrancaService.cancelar(1L, "erro"));
    }

    @Test
    void deveCancelarCobrancaPendente() {
        Cobranca cobranca = new Cobranca();
        cobranca.setId(1L);
        cobranca.setStatus(StatusCobranca.PENDENTE);

        when(cobrancaRepository.findById(1L)).thenReturn(Optional.of(cobranca));

        cobrancaService.cancelar(1L, "Reserva cancelada");

        assertEquals(StatusCobranca.CANCELADA, cobranca.getStatus());
    }

    @Test
    void deveLancar404ParaCobrancaInexistente() {
        when(cobrancaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CobrancaNotFoundException.class, () -> cobrancaService.buscarPorId(99L));
    }

    @Test
    void deveLancarExcecaoQuandoReservaInexistente() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> cobrancaService.salvar(new CobrancaRequestDTO(
                        OrigemCobranca.RESERVA, 99L, null, "x",
                        new BigDecimal("10.00"), null, LocalDate.of(2026, 7, 1), null)));
    }
}
