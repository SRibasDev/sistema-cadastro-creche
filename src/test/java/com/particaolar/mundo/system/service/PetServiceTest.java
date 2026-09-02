package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.domain.service.PetService;
import com.particaolar.mundo.system.dto.request.PetRequestDTO;
import com.particaolar.mundo.system.dto.response.PetResponseDTO;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.mapper.PetMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private TutorRepository tutorRepository;

    @Mock
    private PetMapper petMapper;

    @InjectMocks
    private PetService petService;

    @Test
    void deveSalvarPetComSucesso() {
        Long tutorId = 1L;
        PetRequestDTO requestDTO = new PetRequestDTO("Rex", "Labrador", LocalDate.of(2020, 1, 1));
        Tutor tutor = new Tutor();
        Pet pet = new Pet();
        Pet petSalvo = new Pet();
        petSalvo.setId(1L);
        PetResponseDTO responseDTO = new PetResponseDTO(1L, "Rex", "Labrador", LocalDate.of(2020, 1, 1), tutorId);

        when(tutorRepository.findByIdAndAtivoTrue(tutorId)).thenReturn(Optional.of(tutor));
        when(petMapper.toEntity(requestDTO)).thenReturn(pet);
        when(petRepository.save(pet)).thenReturn(petSalvo);
        when(petMapper.toResponseDTO(petSalvo)).thenReturn(responseDTO);

        PetResponseDTO resultado = petService.salvar(requestDTO, tutorId);

        assertNotNull(resultado);
        assertEquals("Rex", resultado.nome());
        verify(petRepository).save(pet);
    }

    @Test
    void deveLancarExcecaoQuandoTutorNaoEncontradoAoSalvar() {
        Long tutorId = 99L;
        PetRequestDTO requestDTO = new PetRequestDTO("Rex", "Labrador", LocalDate.of(2020, 1, 1));

        when(tutorRepository.findByIdAndAtivoTrue(tutorId)).thenReturn(Optional.empty());

        assertThrows(TutorNotFoundException.class,
                () -> petService.salvar(requestDTO, tutorId));

        verify(petRepository, never()).save(any());
    }

    @Test
    void deveListarTodosOsPetsAtivosPaginados() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Pet> pets = List.of(new Pet(), new Pet());
        Page<Pet> paginaDePets = new PageImpl<>(pets);
        PetResponseDTO responseDTO = new PetResponseDTO(1L, "Rex", "Labrador", LocalDate.now(), 1L);

        when(petRepository.findByAtivoTrue(pageable)).thenReturn(paginaDePets);
        when(petMapper.toResponseDTO(any(Pet.class))).thenReturn(responseDTO);

        Page<PetResponseDTO> resultado = petService.listarTodos(pageable);

        assertEquals(2, resultado.getContent().size());
        verify(petRepository).findByAtivoTrue(pageable);
    }

    @Test
    void deveRetornarPaginaVaziaQuandoNaoHaPetsAtivos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pet> paginaVazia = new PageImpl<>(List.of());

        when(petRepository.findByAtivoTrue(pageable)).thenReturn(paginaVazia);

        Page<PetResponseDTO> resultado = petService.listarTodos(pageable);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarPetPorIdComSucesso() {
        Pet pet = new Pet();
        pet.setId(1L);
        PetResponseDTO responseDTO = new PetResponseDTO(1L, "Rex", "Labrador", LocalDate.of(2020, 1, 1), 1L);

        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pet));
        when(petMapper.toResponseDTO(pet)).thenReturn(responseDTO);

        PetResponseDTO resultado = petService.buscarPetPorPetId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoPetNaoEncontrado() {
        when(petRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class,
                () -> petService.buscarPetPorPetId(99L));
    }

    @Test
    void deveInativarPetComSucesso() {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setAtivo(true);

        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pet));
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        petService.deletar(1L);

        assertFalse(pet.getAtivo());
        verify(petRepository).save(pet);
        verify(petRepository, never()).delete(any());
    }

    @Test
    void deveLancarExcecaoAoDeletarPetInexistente() {
        when(petRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class,
                () -> petService.deletar(99L));

        verify(petRepository, never()).save(any());
    }
}