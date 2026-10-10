package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.domain.entity.ReservaHistorico;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.ReservaHistoricoRepository;
import com.particaolar.mundo.system.domain.repository.ReservaRepository;
import com.particaolar.mundo.system.domain.service.CapacidadeService;
import com.particaolar.mundo.system.domain.service.ReservaService;
import com.particaolar.mundo.system.dto.request.ReagendarRequestDTO;
import com.particaolar.mundo.system.dto.request.ReservaRequestDTO;
import com.particaolar.mundo.system.dto.response.ReservaResponseDTO;
import com.particaolar.mundo.system.enums.StatusReserva;
import com.particaolar.mundo.system.enums.TipoReserva;
import com.particaolar.mundo.system.exception.CapacidadeExcedidaException;
import com.particaolar.mundo.system.exception.PetComReservaNoPeriodoException;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.ReservaNotFoundException;
import com.particaolar.mundo.system.mapper.ReservaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReservaServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private ReservaHistoricoRepository historicoRepository;
    @Mock private PetRepository petRepository;
    @Mock private ReservaMapper reservaMapper;
    @Mock private CapacidadeService capacidadeService;

    @InjectMocks
    private ReservaService reservaService;

    private final LocalDate entrada = LocalDate.of(2026, 3, 10);
    private final LocalDate saida = LocalDate.of(2026, 3, 11);

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

    private ReservaRequestDTO dto() {
        return new ReservaRequestDTO(1L, TipoReserva.CRECHE, entrada, saida,
                LocalTime.of(8, 0), LocalTime.of(18, 0), null);
    }

    private ReservaResponseDTO resposta() {
        return new ReservaResponseDTO(1L, 1L, "Rex", 1L, "Tutor Teste",
                TipoReserva.CRECHE, entrada, saida, LocalTime.of(8, 0), LocalTime.of(18, 0),
                StatusReserva.AGENDADA, null, null, java.time.LocalDateTime.now());
    }
    @Test
    void deveSalvarReservaComSucesso() {
        ReservaRequestDTO dto = dto();
        Reserva salva = new Reserva();
        salva.setId(1L);

        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petValido()));
        when(reservaRepository.buscarPorPetNoPeriodo(eq(1L), anyList(), eq(entrada), eq(saida), eq(null)))
                .thenReturn(List.of());
        doNothing().when(capacidadeService).validarVagas(any(), any(), any(), any());
        when(reservaMapper.toEntity(dto)).thenReturn(salva);
        when(reservaRepository.save(salva)).thenReturn(salva);
        when(reservaMapper.toResponseDTO(salva)).thenReturn(resposta());

        ReservaResponseDTO resultado = reservaService.salvar(dto);

        assertNotNull(resultado);
        assertEquals(StatusReserva.AGENDADA, resultado.status());
        verify(historicoRepository).save(any(ReservaHistorico.class));
    }

    @Test
    void deveLancarExcecaoQuandoPetNaoExiste() {
        when(petRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());
        ReservaRequestDTO dto = new ReservaRequestDTO(99L, TipoReserva.CRECHE, entrada, saida, null, null, null);

        assertThrows(PetNotFoundException.class, () -> reservaService.salvar(dto));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoPetJaTemReservaNoPeriodo() {
        ReservaRequestDTO dto = dto();
        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petValido()));
        when(reservaRepository.buscarPorPetNoPeriodo(eq(1L), anyList(), eq(entrada), eq(saida), eq(null)))
                .thenReturn(List.of(new Reserva()));

        assertThrows(PetComReservaNoPeriodoException.class, () -> reservaService.salvar(dto));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoDataSaidaAnteriorAEntrada() {
        ReservaRequestDTO dto = new ReservaRequestDTO(1L, TipoReserva.CRECHE,
                LocalDate.of(2026, 3, 11), LocalDate.of(2026, 3, 10), null, null, null);

        assertThrows(IllegalArgumentException.class, () -> reservaService.salvar(dto));
        verify(reservaRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoHorarioSaidaAnteriorAEntrada() {
        ReservaRequestDTO dto = new ReservaRequestDTO(1L, TipoReserva.CRECHE, entrada, saida,
                LocalTime.of(18, 0), LocalTime.of(8, 0), null);

        assertThrows(IllegalArgumentException.class, () -> reservaService.salvar(dto));
    }

    @Test
    void devePropagarCapacidadeExcedida() {
        ReservaRequestDTO dto = dto();
        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petValido()));
        when(reservaRepository.buscarPorPetNoPeriodo(eq(1L), anyList(), eq(entrada), eq(saida), eq(null)))
                .thenReturn(List.of());
        doThrow(new CapacidadeExcedidaException(entrada, 50))
                .when(capacidadeService).validarVagas(any(), any(), any(), any());

        assertThrows(CapacidadeExcedidaException.class, () -> reservaService.salvar(dto));
        verify(reservaRepository, never()).save(any());
    }
    @Test
    void deveReagendarComSucessoERegistrarHistorico() {
        Reserva existente = new Reserva();
        existente.setId(1L);
        existente.setPet(petValido());
        existente.setTipo(TipoReserva.CRECHE);
        existente.setDataEntrada(entrada);
        existente.setDataSaida(saida);
        existente.setStatus(StatusReserva.AGENDADA);

        LocalDate novaEntrada = LocalDate.of(2026, 3, 15);
        LocalDate novaSaida = LocalDate.of(2026, 3, 16);
        ReagendarRequestDTO dto = new ReagendarRequestDTO("Pet precisou", novaEntrada, novaSaida, null, null);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reservaRepository.buscarPorPetNoPeriodo(eq(1L), anyList(), eq(novaEntrada), eq(novaSaida), eq(1L)))
                .thenReturn(List.of());
        doNothing().when(capacidadeService).validarVagas(any(), eq(novaEntrada), eq(novaSaida), eq(1L));
        when(reservaMapper.toResponseDTO(existente)).thenReturn(resposta());

        ReservaResponseDTO resultado = reservaService.reagendar(1L, dto);

        assertNotNull(resultado);
        assertEquals(novaEntrada, existente.getDataEntrada());
        assertEquals(novaSaida, existente.getDataSaida());
        verify(historicoRepository).save(any(ReservaHistorico.class));
    }

    @Test
    void naoDeveReagendarReservaConcluida() {
        Reserva existente = new Reserva();
        existente.setId(1L);
        existente.setPet(petValido());
        existente.setStatus(StatusReserva.CONCLUIDA);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class,
                () -> reservaService.reagendar(1L, new ReagendarRequestDTO("motivo", null, null, null, null)));
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void deveCancelarReservaComMotivo() {
        Reserva existente = new Reserva();
        existente.setId(1L);
        existente.setPet(petValido());
        existente.setDataEntrada(entrada);
        existente.setDataSaida(saida);
        existente.setStatus(StatusReserva.AGENDADA);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(existente));

        reservaService.cancelar(1L, "Tutor desmarcou");

        assertEquals(StatusReserva.CANCELADA, existente.getStatus());
        assertEquals("Tutor desmarcou", existente.getMotivoCancelamento());
        verify(historicoRepository).save(any(ReservaHistorico.class));
    }

    @Test
    void naoDeveCancelarReservaConcluida() {
        Reserva existente = new Reserva();
        existente.setId(1L);
        existente.setPet(petValido());
        existente.setStatus(StatusReserva.CONCLUIDA);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class, () -> reservaService.cancelar(1L, "x"));
    }

    @Test
    void transicaoDeStatusValida() {
        Reserva existente = new Reserva();
        existente.setId(1L);
        existente.setPet(petValido());
        existente.setStatus(StatusReserva.AGENDADA);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(reservaMapper.toResponseDTO(existente)).thenReturn(resposta());

        reservaService.atualizarStatus(1L, StatusReserva.EM_ANDAMENTO);

        assertEquals(StatusReserva.EM_ANDAMENTO, existente.getStatus());
    }

    @Test
    void transicaoDeStatusInvalidaLanca() {
        Reserva existente = new Reserva();
        existente.setId(1L);
        existente.setPet(petValido());
        existente.setStatus(StatusReserva.CONCLUIDA);

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class,
                () -> reservaService.atualizarStatus(1L, StatusReserva.AGENDADA));
        verify(historicoRepository, never()).save(any());
    }

    @Test
    void deveLancar404ParaReservaInexistente() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ReservaNotFoundException.class, () -> reservaService.buscarPorId(99L));
    }
}