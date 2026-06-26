package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.domain.service.PetService;
import com.particaolar.mundo.system.dto.petDTO.PetRequestDTO;
import com.particaolar.mundo.system.dto.petDTO.PetResponseDTO;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.mapper.PetMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

        when(tutorRepository.findById(tutorId)).thenReturn(Optional.of(tutor));
        when(petMapper.toEntity(requestDTO)).thenReturn(pet);
        when(petRepository.save(pet)).thenReturn(petSalvo);
        when(petMapper.toResponseDTO(petSalvo)).thenReturn(responseDTO);

        PetResponseDTO resultado = petService.salvar(requestDTO, tutorId);

        assertNotNull(resultado);
        assertEquals("Rex", resultado.nome());
        verify(petRepository).save(pet); // confirma que o save foi chamado
    }

    @Test
    void deveLancarExcecaoQuandoTutorNaoEncontradoAoSalvar() {
        Long tutorId = 99L;
        PetRequestDTO requestDTO = new PetRequestDTO("Rex", "Labrador", LocalDate.of(2020, 1, 1));

        when(tutorRepository.findById(tutorId)).thenReturn(Optional.empty());

        assertThrows(TutorNotFoundException.class,
                () -> petService.salvar(requestDTO, tutorId));

        verify(petRepository, never()).save(any()); // confirma que não tentou salvar
    }

    @Test
    void deveListarTodosOsPets() {
        List<Pet> listaPets = List.of(new Pet(), new Pet());
        when(petRepository.findAll()).thenReturn(listaPets);
        when(petMapper.toResponseDTO(any(Pet.class))).thenReturn(
                new PetResponseDTO(1L, "Rex", "Labrador", LocalDate.of(2020, 1, 1), 1L)
        );

        List<PetResponseDTO> resultado = petService.listarTodos();

        assertEquals(2, resultado.size());
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaPets() {
        when(petRepository.findAll()).thenReturn(List.of());

        List<PetResponseDTO> resultado = petService.listarTodos();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void deveBuscarPetPorIdComSucesso() {
        Pet pet = new Pet();
        pet.setId(1L);
        PetResponseDTO responseDTO = new PetResponseDTO(1L, "Rex", "Labrador", LocalDate.of(2020, 1, 1), 1L);

        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petMapper.toResponseDTO(pet)).thenReturn(responseDTO);

        PetResponseDTO resultado = petService.buscarPetPorPetId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
    }

    @Test
    void deveLancarExcecaoQuandoPetNaoEncontrado() {
        when(petRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class,
                () -> petService.buscarPetPorPetId(99L));
    }

    @Test
    void deveDeletarPetComSucesso() {
        Pet pet = new Pet();
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        petService.deletar(1L);

        verify(petRepository).delete(pet); // confirma que deletou
    }

    @Test
    void deveLancarExcecaoAoDeletarPetInexistente() {
        when(petRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class,
                () -> petService.deletar(99L));

        verify(petRepository, never()).delete(any()); // confirma que não tentou deletar
    }
}