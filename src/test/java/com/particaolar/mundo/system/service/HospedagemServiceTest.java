package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.service.HospedagemService;
import com.particaolar.mundo.system.dto.request.HospedagemRequestDTO;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import com.particaolar.mundo.system.exception.HospedagemNotFoundException;
import com.particaolar.mundo.system.exception.PetJaHospedadoException;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.mapper.HospedagemMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HospedagemServiceTest {

    @Mock
    private HospedagemRepository hospedagemRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private HospedagemMapper hospedagemMapper;

    @InjectMocks
    private HospedagemService hospedagemService;

    // dados reutilizados nos testes
    private final LocalDate entrada = LocalDate.of(2025, 7, 1);
    private final LocalDate saida = LocalDate.of(2025, 7, 5);

    private HospedagemResponseDTO responseDTO() {
        return new HospedagemResponseDTO(
                1L, 1L, "Rex", "João",
                entrada, saida,
                StatusHospedagem.AGENDADA,
                null,
                LocalDateTime.now()
        );
    }

    @Test
    void deveSalvarHospedagemComSucesso() {
        HospedagemRequestDTO dto = new HospedagemRequestDTO(1L, entrada, saida, null);
        Pet pet = new Pet();
        Hospedagem hospedagem = new Hospedagem();
        Hospedagem hospedagemSalva = new Hospedagem();

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(hospedagemRepository.existsByPetIdAndStatusIn(eq(1L), any())).thenReturn(false);
        when(hospedagemMapper.toEntity(dto)).thenReturn(hospedagem);
        when(hospedagemRepository.save(hospedagem)).thenReturn(hospedagemSalva);
        when(hospedagemMapper.toResponseDTO(hospedagemSalva)).thenReturn(responseDTO());

        HospedagemResponseDTO resultado = hospedagemService.salvar(dto);

        assertNotNull(resultado);
        assertEquals(StatusHospedagem.AGENDADA, resultado.status());
        verify(hospedagemRepository).save(hospedagem);
    }

    @Test
    void deveLancarExcecaoQuandoPetNaoEncontradoAoSalvar() {
        HospedagemRequestDTO dto = new HospedagemRequestDTO(99L, entrada, saida, null);

        when(petRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class,
                () -> hospedagemService.salvar(dto));

        // não chegou a checar hospedagem nem salvar
        verify(hospedagemRepository, never()).existsByPetIdAndStatusIn(any(), any());
        verify(hospedagemRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoPetJaHospedado() {
        HospedagemRequestDTO dto = new HospedagemRequestDTO(1L, entrada, saida, null);
        Pet pet = new Pet();

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(hospedagemRepository.existsByPetIdAndStatusIn(eq(1L), any())).thenReturn(true);

        assertThrows(PetJaHospedadoException.class,
                () -> hospedagemService.salvar(dto));

        verify(hospedagemRepository, never()).save(any());
    }

    @Test
    void deveListarTodasAsHospedagens() {
        List<Hospedagem> hospedagens = List.of(new Hospedagem(), new Hospedagem());

        when(hospedagemRepository.findAll()).thenReturn(hospedagens);
        when(hospedagemMapper.toResponseDTO(any(Hospedagem.class))).thenReturn(responseDTO());

        List<HospedagemResponseDTO> resultado = hospedagemService.listarTodas();

        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaHospedagens() {
        when(hospedagemRepository.findAll()).thenReturn(List.of());

        List<HospedagemResponseDTO> resultado = hospedagemService.listarTodas();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarHospedagemPorIdComSucesso() {
        Hospedagem hospedagem = new Hospedagem();

        when(hospedagemRepository.findById(1L)).thenReturn(Optional.of(hospedagem));
        when(hospedagemMapper.toResponseDTO(hospedagem)).thenReturn(responseDTO());

        HospedagemResponseDTO resultado = hospedagemService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoHospedagemNaoEncontrada() {
        when(hospedagemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(HospedagemNotFoundException.class,
                () -> hospedagemService.buscarPorId(99L));
    }

    @Test
    void deveBuscarHospedagensPorPetIdComSucesso() {
        List<Hospedagem> hospedagens = List.of(new Hospedagem());

        when(petRepository.existsById(1L)).thenReturn(true);
        when(hospedagemRepository.findByPetId(1L)).thenReturn(hospedagens);
        when(hospedagemMapper.toResponseDTO(any(Hospedagem.class))).thenReturn(responseDTO());

        List<HospedagemResponseDTO> resultado = hospedagemService.buscarPorPetId(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveLancarExcecaoQuandoPetNaoEncontradoAoBuscarPorPetId() {
        when(petRepository.existsById(99L)).thenReturn(false);

        assertThrows(PetNotFoundException.class,
                () -> hospedagemService.buscarPorPetId(99L));

        verify(hospedagemRepository, never()).findByPetId(any());
    }

    @Test
    void deveAtualizarStatusDaHospedagemComSucesso() {
        Hospedagem hospedagem = new Hospedagem();
        hospedagem.setStatus(StatusHospedagem.AGENDADA);

        when(hospedagemRepository.findById(1L)).thenReturn(Optional.of(hospedagem));
        when(hospedagemMapper.toResponseDTO(hospedagem)).thenReturn(
                new HospedagemResponseDTO(1L, 1L, "Rex", "João",
                        entrada, saida, StatusHospedagem.EM_ANDAMENTO, null, LocalDateTime.now())
        );

        HospedagemResponseDTO resultado = hospedagemService.atualizarStatus(1L, StatusHospedagem.EM_ANDAMENTO);

        assertEquals(StatusHospedagem.EM_ANDAMENTO, hospedagem.getStatus()); // verifica que setou
        assertNotNull(resultado);
    }

    @Test
    void deveLancarExcecaoAoAtualizarStatusDeHospedagemInexistente() {
        when(hospedagemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(HospedagemNotFoundException.class,
                () -> hospedagemService.atualizarStatus(99L, StatusHospedagem.EM_ANDAMENTO));
    }

    @Test
    void deveCancelarHospedagemComSucesso() {
        Hospedagem hospedagem = new Hospedagem();
        hospedagem.setStatus(StatusHospedagem.AGENDADA);

        when(hospedagemRepository.findById(1L)).thenReturn(Optional.of(hospedagem));

        hospedagemService.cancelar(1L);

        assertEquals(StatusHospedagem.CANCELADA, hospedagem.getStatus()); // verifica que mudou
    }

    @Test
    void deveLancarExcecaoAoCancelarHospedagemInexistente() {
        when(hospedagemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(HospedagemNotFoundException.class,
                () -> hospedagemService.cancelar(99L));
    }
}