package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.PetPertence;
import com.particaolar.mundo.system.domain.repository.PetPertenceRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.service.PetPertenceService;
import com.particaolar.mundo.system.dto.request.PetPertenceRequestDTO;
import com.particaolar.mundo.system.dto.response.PetPertenceResponseDTO;
import com.particaolar.mundo.system.enums.TipoPertence;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.PetPertenceNotFoundException;
import com.particaolar.mundo.system.mapper.PetPertenceMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PetPertenceServiceTest {

    @Mock private PetPertenceRepository pertenceRepository;
    @Mock private PetRepository petRepository;
    @Mock private PetPertenceMapper pertenceMapper;

    @InjectMocks
    private PetPertenceService petPertenceService;

    private PetPertenceRequestDTO dto() {
        return new PetPertenceRequestDTO(TipoPertence.RACAO, "Ração Premium 15kg",
                "1 saco", 15, "Sem corantes");
    }

    private PetPertenceResponseDTO resposta() {
        return new PetPertenceResponseDTO(1L, 1L, TipoPertence.RACAO, "Ração Premium 15kg",
                "1 saco", 15, "Sem corantes", true, LocalDateTime.now());
    }

    @Test
    void deveSalvarPertenceComSucesso() {
        Pet pet = new Pet();
        pet.setId(1L);
        PetPertenceRequestDTO dto = dto();
        PetPertence pertence = new PetPertence();
        PetPertence salvo = new PetPertence();
        salvo.setId(1L);

        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pet));
        when(pertenceMapper.toEntity(dto)).thenReturn(pertence);
        when(pertenceRepository.save(pertence)).thenReturn(salvo);
        when(pertenceMapper.toResponseDTO(salvo)).thenReturn(resposta());

        PetPertenceResponseDTO resultado = petPertenceService.salvar(1L, dto);

        assertNotNull(resultado);
        assertEquals(TipoPertence.RACAO, resultado.tipo());
        verify(pertenceRepository).save(pertence);
    }

    @Test
    void deveLancarExcecaoQuandoPetNaoExisteAoCriarPertence() {
        when(petRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class, () -> petPertenceService.salvar(99L, dto()));
        verify(pertenceRepository, never()).save(any());
    }

    @Test
    void deveListarPertencesAtivosDoPet() {
        PetPertence p = new PetPertence();
        when(petRepository.existsById(1L)).thenReturn(true);
        when(pertenceRepository.findByPetIdAndAtivoTrue(1L)).thenReturn(List.of(p));
        when(pertenceMapper.toResponseDTO(p)).thenReturn(resposta());

        List<PetPertenceResponseDTO> resultado = petPertenceService.listarPorPet(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    void deveAtualizarPertence() {
        PetPertence existente = new PetPertence();
        existente.setId(1L);
        existente.setPet(new Pet());
        PetPertenceRequestDTO dto = dto();

        when(pertenceRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(existente));
        when(pertenceMapper.toResponseDTO(existente)).thenReturn(resposta());

        PetPertenceResponseDTO resultado = petPertenceService.atualizar(1L, dto);

        assertNotNull(resultado);
        verify(pertenceMapper).updateEntityFromDTO(dto, existente);
    }

    @Test
    void deveDesativarPertence() {
        PetPertence existente = new PetPertence();
        existente.setId(1L);
        existente.setAtivo(true);

        when(pertenceRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(existente));

        petPertenceService.deletar(1L);

        assertFalse(existente.getAtivo());
        verify(pertenceRepository, never()).delete(any());
    }

    @Test
    void deveLancar404ParaPertenceInexistente() {
        when(pertenceRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(PetPertenceNotFoundException.class,
                () -> petPertenceService.atualizar(99L, dto()));
    }
}
