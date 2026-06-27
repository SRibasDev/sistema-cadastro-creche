package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.domain.service.TutorService;
import com.particaolar.mundo.system.dto.request.TutorRequestDTO;
import com.particaolar.mundo.system.dto.response.TutorResponseDTO;
import com.particaolar.mundo.system.exception.TutorCpfAlreadyExistsException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.exception.TutorPhoneNumberAlreadyExistsException;
import com.particaolar.mundo.system.mapper.TutorMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    void deveListarTodosOsTutoresAtivosPaginados() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Tutor> tutores = List.of(new Tutor(), new Tutor());
        Page<Tutor> paginaDeTutores = new PageImpl<>(tutores); // Converte List para Page
        TutorResponseDTO responseDTO = new TutorResponseDTO(1L, "João", "11999999999");

        // Simula o novo método do repositório
        when(tutorRepository.findByAtivoTrue(pageable)).thenReturn(paginaDeTutores);
        when(tutorMapper.toResponseDTO(any(Tutor.class))).thenReturn(responseDTO);

        Page<TutorResponseDTO> resultado = tutorService.listarTodos(pageable);

        // Verifica os itens usando o getContent() da página
        assertEquals(2, resultado.getContent().size());
        verify(tutorRepository).findByAtivoTrue(pageable);
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaTutoresAtivos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Tutor> paginaVazia = new PageImpl<>(List.of());

        when(tutorRepository.findByAtivoTrue(pageable)).thenReturn(paginaVazia);

        Page<TutorResponseDTO> resultado = tutorService.listarTodos(pageable);

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
        tutor.setCpf("123.456.789-00");       // mesmo CPF — não vai checar duplicata
        tutor.setTelefone("11988888888");      // mesmo telefone — não vai checar duplicata
        TutorResponseDTO responseDTO = new TutorResponseDTO(1L, "João Atualizado", "11988888888");

        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(tutorMapper.toResponseDTO(tutor)).thenReturn(responseDTO);

        TutorResponseDTO resultado = tutorService.atualizar(requestDTO, 1L);

        assertNotNull(resultado);
        assertEquals("João Atualizado", resultado.nome());
        verify(tutorMapper).updateEntityFromDTO(requestDTO, tutor);
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
    void deveInativarTutorComSucesso() {
        Tutor tutor = new Tutor();
        tutor.setId(1L);
        tutor.setAtivo(true); // O Tutor começa ativo

        when(tutorRepository.findById(1L)).thenReturn(Optional.of(tutor));
        when(tutorRepository.save(any(Tutor.class))).thenReturn(tutor);

        tutorService.deletar(1L);

        // Garante que o status virou false
        assertFalse(tutor.getAtivo());
        // Garante que o sistema salvou a alteração em vez de deletar
        verify(tutorRepository).save(tutor);
        // Garante que o delete físico do Hibernate nunca foi chamado
        verify(tutorRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoAoDeletarTutorInexistente() {
        when(tutorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TutorNotFoundException.class,
                () -> tutorService.deletar(99L));

        verify(tutorRepository, never()).delete(any());
    }
}