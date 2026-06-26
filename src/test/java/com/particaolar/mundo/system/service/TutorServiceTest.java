package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.domain.service.TutorService;
import com.particaolar.mundo.system.dto.tutorDTO.TutorRequestDTO;
import com.particaolar.mundo.system.dto.tutorDTO.TutorResponseDTO;
import com.particaolar.mundo.system.exception.TutorCpfAlreadyExistsException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.exception.TutorPhoneNumberAlreadyExistsException;
import com.particaolar.mundo.system.mapper.TutorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TutorServiceTest {

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private TutorMapper tutorMapper;

    @InjectMocks
    private TutorService tutorService;

    @Test
    void deveSalvarTutorComSucesso() {
        TutorRequestDTO requestDTO = new TutorRequestDTO("João", "11999999999", "123.456.789-00");
        Tutor tutor = new Tutor();
        Tutor tutorSalvo = new Tutor();
        tutorSalvo.setId(1L);
        TutorResponseDTO responseDTO = new TutorResponseDTO(1L, "João", "11999999999");

        when(tutorRepository.existsByCpf(requestDTO.cpf())).thenReturn(false);
        when(tutorRepository.existsByTelefone(requestDTO.telefone())).thenReturn(false);
        when(tutorMapper.toEntity(requestDTO)).thenReturn(tutor);
        when(tutorRepository.save(tutor)).thenReturn(tutorSalvo);
        when(tutorMapper.toResponseDTO(tutorSalvo)).thenReturn(responseDTO);

        TutorResponseDTO resultado = tutorService.salvarTutor(requestDTO);

        assertNotNull(resultado);
        assertEquals("João", resultado.nome());
        verify(tutorRepository).save(tutor);
    }

    @Test
    void deveLancarExcecaoQuandoCpfJaCadastrado() {
        TutorRequestDTO requestDTO = new TutorRequestDTO("João", "11999999999","102.065.345-4");

        when(tutorRepository.existsByCpf(requestDTO.cpf())).thenReturn(true);

        assertThrows(TutorCpfAlreadyExistsException.class,
                () -> tutorService.salvarTutor(requestDTO));

        // não chegou nem a checar telefone nem a salvar
        verify(tutorRepository, never()).existsByTelefone(any());
        verify(tutorRepository, never()).save(any());
    }

    @Test
    void deveLancarExcecaoQuandoTelefoneJaCadastrado() {
        TutorRequestDTO requestDTO = new TutorRequestDTO("João", "11999999999", "123.456.789-00");

        when(tutorRepository.existsByCpf(requestDTO.cpf())).thenReturn(false);
        when(tutorRepository.existsByTelefone(requestDTO.telefone())).thenReturn(true);

        assertThrows(TutorPhoneNumberAlreadyExistsException.class,
                () -> tutorService.salvarTutor(requestDTO));

        verify(tutorRepository, never()).save(any());
    }

    @Test
    void deveListarTodosOsTutores() {
        List<Tutor> tutores = List.of(new Tutor(), new Tutor());
        TutorResponseDTO responseDTO = new TutorResponseDTO(1L, "João", "11999999999");

        when(tutorRepository.findAll()).thenReturn(tutores);
        when(tutorMapper.toResponseDTO(any(Tutor.class))).thenReturn(responseDTO);

        List<TutorResponseDTO> resultado = tutorService.listarTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaTutores() {
        when(tutorRepository.findAll()).thenReturn(List.of());

        List<TutorResponseDTO> resultado = tutorService.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarTutorPorIdComSucesso() {
        Tutor tutor = new Tutor();
        tutor.setId(1L);
        TutorResponseDTO responseDTO = new TutorResponseDTO(1L, "João", "11999999999");

        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(tutorMapper.toResponseDTO(tutor)).thenReturn(responseDTO);

        TutorResponseDTO resultado = tutorService.buscarPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoTutorNaoEncontrado() {
        when(tutorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TutorNotFoundException.class,
                () -> tutorService.buscarPorId(99L));
    }

    @Test
    void deveAtualizarTutorComSucesso() {
        TutorRequestDTO requestDTO = new TutorRequestDTO("João Atualizado", "11988888888", "123.456.789-00");
        Tutor tutor = new Tutor();
        tutor.setId(1L);
        TutorResponseDTO responseDTO = new TutorResponseDTO(1L, "João Atualizado", "11988888888");

        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(tutorRepository.save(tutor)).thenReturn(tutor);
        when(tutorMapper.toResponseDTO(tutor)).thenReturn(responseDTO);

        TutorResponseDTO resultado = tutorService.atualizar(requestDTO, 1L);

        assertNotNull(resultado);
        assertEquals("João Atualizado", resultado.nome());
        verify(tutorMapper).updateEntityFromDTO(requestDTO, tutor);
        verify(tutorRepository).save(tutor);
    }

    @Test
    void deveLancarExcecaoAoAtualizarTutorInexistente() {
        TutorRequestDTO requestDTO = new TutorRequestDTO("João", "11999999999", "123.456.789-00");

        when(tutorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TutorNotFoundException.class,
                () -> tutorService.atualizar(requestDTO, 99L));

        verify(tutorRepository, never()).save(any());
    }

    @Test
    void deveDeletarTutorComSucesso() {
        Tutor tutor = new Tutor();
        tutor.setId(1L);

        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));

        tutorService.deletar(1L);

        verify(tutorRepository).delete(tutor);
    }

    @Test
    void deveLancarExcecaoAoDeletarTutorInexistente() {
        when(tutorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TutorNotFoundException.class,
                () -> tutorService.deletar(99L));

        verify(tutorRepository, never()).delete(any());
    }
}