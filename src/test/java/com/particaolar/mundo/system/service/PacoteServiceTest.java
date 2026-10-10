package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Pacote;
import com.particaolar.mundo.system.domain.entity.PacoteComprado;
import com.particaolar.mundo.system.domain.entity.PacoteConsumo;
import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.domain.entity.Servico;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.DescontoRepository;
import com.particaolar.mundo.system.domain.repository.PacoteCompradoRepository;
import com.particaolar.mundo.system.domain.repository.PacoteConsumoRepository;
import com.particaolar.mundo.system.domain.repository.PacoteRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.service.PacoteService;
import com.particaolar.mundo.system.domain.service.ServicoService;
import com.particaolar.mundo.system.dto.request.PacoteCompraRequestDTO;
import com.particaolar.mundo.system.dto.request.PacoteRequestDTO;
import com.particaolar.mundo.system.dto.response.PacoteCompradoResponseDTO;
import com.particaolar.mundo.system.dto.response.PacoteResponseDTO;
import com.particaolar.mundo.system.enums.UnidadeServico;
import com.particaolar.mundo.system.exception.PacoteNotFoundException;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.SaldoInsuficienteException;
import com.particaolar.mundo.system.mapper.PacoteMapper;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PacoteServiceTest {

    @Mock private PacoteRepository pacoteRepository;
    @Mock private PacoteCompradoRepository pacoteCompradoRepository;
    @Mock private PacoteConsumoRepository consumoRepository;
    @Mock private PetRepository petRepository;
    @Mock private DescontoRepository descontoRepository;
    @Mock private ServicoService servicoService;
    @Mock private PacoteMapper pacoteMapper;

    @InjectMocks
    private PacoteService pacoteService;

    private final Clock clock = Clock.fixed(
            ZonedDateTime.of(2026, 5, 1, 10, 0, 0, 0, ZoneId.of("America/Sao_Paulo")).toInstant(),
            ZoneId.of("America/Sao_Paulo"));

    private Pet petValido() {
        Tutor tutor = new Tutor();
        tutor.setId(1L);
        tutor.setNome("Tutor Teste");
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Rex");
        pet.setTutor(tutor);
        return pet;
    }

    private Servico servicoValido() {
        Servico servico = new Servico();
        servico.setId(1L);
        servico.setNome("Creche diária");
        servico.setPreco(new BigDecimal("80.00"));
        servico.setUnidade(UnidadeServico.DIARIA);
        return servico;
    }

    private Pacote pacoteValido() {
        Pacote pacote = new Pacote();
        pacote.setId(1L);
        pacote.setNome("Pacote 10 diárias");
        pacote.setServico(servicoValido());
        pacote.setQuantidade(10);
        pacote.setPrecoTotal(new BigDecimal("700.00"));
        pacote.setValidadeDias(90);
        pacote.setAtivo(true);
        return pacote;
    }

    private PacoteComprado compradoValido(int saldo) {
        PacoteComprado comprado = new PacoteComprado();
        comprado.setId(1L);
        comprado.setPacote(pacoteValido());
        comprado.setPet(petValido());
        comprado.setSaldo(saldo);
        comprado.setValorPago(new BigDecimal("700.00"));
        comprado.setValidade(LocalDate.of(2026, 8, 1));
        comprado.setAtivo(true);
        return comprado;
    }

    @org.junit.jupiter.api.BeforeEach
    void setUp() throws Exception {
        Field clockField = PacoteService.class.getDeclaredField("clock");
        clockField.setAccessible(true);
        clockField.set(pacoteService, clock);
        Field debitoField = PacoteService.class.getDeclaredField("debitoAutomatico");
        debitoField.setAccessible(true);
        debitoField.set(pacoteService, true);
    }
    @Test
    void deveSalvarPacoteComSucesso() {
        PacoteRequestDTO dto = new PacoteRequestDTO("Pacote 10 diárias", "Creche",
                1L, 10, new BigDecimal("700.00"), 90, null);

        when(pacoteRepository.existsByNome("Pacote 10 diárias")).thenReturn(false);
        when(servicoService.buscarEntidade(1L)).thenReturn(servicoValido());
        when(pacoteMapper.toEntity(any(PacoteRequestDTO.class))).thenReturn(new Pacote());
        when(pacoteRepository.save(any(Pacote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pacoteMapper.toResponseDTO(any())).thenReturn(new PacoteResponseDTO(
                1L, "Pacote 10 diárias", "Creche", 1L, "Creche diária", UnidadeServico.DIARIA,
                10, new BigDecimal("700.00"), 90, null, null, true, java.time.LocalDateTime.now()));

        PacoteResponseDTO resultado = pacoteService.salvar(dto);

        assertNotNull(resultado);
        assertEquals(10, resultado.quantidade());
        verify(pacoteRepository).save(any(Pacote.class));
    }

    @Test
    void deveComprarPacoteGerandoSaldoEValidade() {
        PacoteCompraRequestDTO dto = new PacoteCompraRequestDTO(1L, null);

        when(pacoteRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pacoteValido()));
        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petValido()));
        when(pacoteCompradoRepository.save(any(PacoteComprado.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pacoteMapper.toCompradoResponseDTO(any())).thenReturn(new PacoteCompradoResponseDTO(
                1L, 1L, "Pacote 10 diárias", UnidadeServico.DIARIA, 1L, "Rex",
                10, new BigDecimal("700.00"), LocalDate.of(2026, 7, 30), true,
                java.time.LocalDateTime.now()));

        PacoteCompradoResponseDTO resultado = pacoteService.comprar(1L, dto);

        assertEquals(10, resultado.saldo());
        // validade = 2026-05-01 + 90 dias
        assertEquals(LocalDate.of(2026, 7, 30), resultado.validade());
    }

    @Test
    void deveDebitarSaldoNaPresenca() {
        Reserva reserva = new Reserva();
        reserva.setId(1L);

        when(pacoteCompradoRepository.buscarComSaldo(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(compradoValido(3)));
        when(pacoteCompradoRepository.findWithLockById(1L)).thenReturn(Optional.of(compradoValido(3)));

        Long pacoteConsumido = pacoteService.debitarSaldo(1L, reserva);

        assertEquals(1L, pacoteConsumido);
        verify(consumoRepository).save(any(PacoteConsumo.class));
    }

    @Test
    void debitarDuasVezasZeraSaldoEInativaPacote() {
        PacoteComprado comSaldo1 = compradoValido(1);

        when(pacoteCompradoRepository.buscarComSaldo(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(comSaldo1));
        when(pacoteCompradoRepository.findWithLockById(1L)).thenReturn(Optional.of(comSaldo1));

        pacoteService.debitarSaldo(1L, new Reserva());

        assertEquals(0, comSaldo1.getSaldo());
        assertFalse(comSaldo1.getAtivo());
    }

    @Test
    void debitarSemSaldoDisponivelRetornaNull() {
        when(pacoteCompradoRepository.buscarComSaldo(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of());

        Long resultado = pacoteService.debitarSaldo(1L, new Reserva());

        assertNull(resultado);
        verify(consumoRepository, never()).save(any());
    }

    @Test
    void saldoZeroLancaSaldoInsuficiente() {
        PacoteComprado zerado = compradoValido(0);
        when(pacoteCompradoRepository.buscarComSaldo(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(zerado));
        when(pacoteCompradoRepository.findWithLockById(1L)).thenReturn(Optional.of(zerado));

        assertThrows(SaldoInsuficienteException.class,
                () -> pacoteService.debitarSaldo(1L, new Reserva()));
    }

    @Test
    void deveLancar404ParaPacoteInexistente() {
        when(pacoteRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(PacoteNotFoundException.class, () -> pacoteService.buscarPorId(99L));
    }

    @Test
    void deveLancar404AoComprarParaPetInexistente() {
        when(pacoteRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pacoteValido()));
        when(petRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class,
                () -> pacoteService.comprar(1L, new PacoteCompraRequestDTO(99L, null)));
    }
}
