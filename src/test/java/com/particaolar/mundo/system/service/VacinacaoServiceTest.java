package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Vacinacao;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.VacinacaoRepository;
import com.particaolar.mundo.system.domain.service.VacinacaoService;
import com.particaolar.mundo.system.dto.request.VacinacaoRequestDTO;
import com.particaolar.mundo.system.dto.response.VacinacaoResponseDTO;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.mapper.VacinacaoMapper;
import org.junit.jupiter.api.DisplayName;
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
class VacinacaoServiceTest {

    @Mock
    private VacinacaoRepository vacinacaoRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private VacinacaoMapper vacinacaoMapper;

    @InjectMocks
    private VacinacaoService vacinacaoService;

    private VacinacaoRequestDTO dtoValido() {
        return new VacinacaoRequestDTO(
                "V10",
                LocalDate.now().minusDays(10),
                LocalDate.now().plusDays(355),
                "LOTE-123",
                "Dra. Ana",
                "Aplicada sem reacoes");
    }

    private Pet petAtivo() {
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Rex");
        pet.setAtivo(true);
        return pet;
    }

    private Vacinacao entidadeSalva() {
        Vacinacao v = new Vacinacao();
        v.setId(1L);
        v.setPet(petAtivo());
        v.setNomeVacina("V10");
        v.setDataAplicacao(LocalDate.now().minusDays(10));
        v.setDataVencimento(LocalDate.now().plusDays(355));
        v.setAtivo(true);
        v.setCriadoEm(LocalDateTime.now());
        return v;
    }

    private VacinacaoResponseDTO resposta() {
        return new VacinacaoResponseDTO(1L, 1L, "Rex", "V10",
                LocalDate.now().minusDays(10), LocalDate.now().plusDays(355),
                "LOTE-123", "Dra. Ana", null, "EM_DIA", true, LocalDateTime.now());
    }

    @Test
    @DisplayName("Deve registrar vacinacao com sucesso")
    void deveRegistrarComSucesso() {
        VacinacaoRequestDTO dto = dtoValido();
        Pet pet = petAtivo();
        Vacinacao entity = new Vacinacao();
        Vacinacao salva = entidadeSalva();

        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pet));
        when(vacinacaoRepository.findByPetIdAndAtivoTrueOrderByDataVencimentoAsc(1L)).thenReturn(List.of());
        when(vacinacaoMapper.toEntity(dto)).thenReturn(entity);
        when(vacinacaoRepository.save(entity)).thenReturn(salva);
        when(vacinacaoMapper.toResponseDTO(salva)).thenReturn(resposta());

        VacinacaoResponseDTO resultado = vacinacaoService.registrar(1L, dto);

        assertNotNull(resultado);
        assertEquals("V10", resultado.nomeVacina());
        assertEquals("EM_DIA", resultado.situacao());
        verify(vacinacaoRepository).save(entity);
    }

    @Test
    @DisplayName("Deve recusar vencimento anterior a aplicacao")
    void deveRecusarVencimentoAnteriorAplicacao() {
        VacinacaoRequestDTO dto = new VacinacaoRequestDTO(
                "Antirrabica", LocalDate.now().minusDays(5), LocalDate.now().minusDays(10),
                null, null, null);

        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petAtivo()));

        assertThrows(IllegalArgumentException.class, () -> vacinacaoService.registrar(1L, dto));
        verify(vacinacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve recusar aplicacao futura")
    void deveRecusarAplicacaoFutura() {
        VacinacaoRequestDTO dto = new VacinacaoRequestDTO(
                "V10", LocalDate.now().plusDays(1), LocalDate.now().plusDays(366),
                null, null, null);

        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petAtivo()));

        assertThrows(IllegalArgumentException.class, () -> vacinacaoService.registrar(1L, dto));
        verify(vacinacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve recusar registro duplicado da mesma vacina no mesmo dia")
    void deveRecusarDuplicada() {
        VacinacaoRequestDTO dto = dtoValido();
        Vacinacao existente = entidadeSalva();
        existente.setDataAplicacao(dto.dataAplicacao());

        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petAtivo()));
        when(vacinacaoRepository.findByPetIdAndAtivoTrueOrderByDataVencimentoAsc(1L))
                .thenReturn(List.of(existente));

        assertThrows(IllegalArgumentException.class, () -> vacinacaoService.registrar(1L, dto));
        verify(vacinacaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lancar excecao quando pet nao existe")
    void deveLancarExcecaoQuandoPetNaoExiste() {
        when(petRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class, () -> vacinacaoService.registrar(99L, dtoValido()));
    }

    @Test
    @DisplayName("Deve listar vacinas do pet ordenadas por vencimento")
    void deveListarDoPet() {
        when(petRepository.existsById(1L)).thenReturn(true);
        when(vacinacaoRepository.findByPetIdAndAtivoTrueOrderByDataVencimentoAsc(1L))
                .thenReturn(List.of(entidadeSalva()));
        when(vacinacaoMapper.toResponseDTO(any(Vacinacao.class))).thenReturn(resposta());

        List<VacinacaoResponseDTO> resultado = vacinacaoService.listarDoPet(1L);

        assertEquals(1, resultado.size());
        assertEquals("V10", resultado.get(0).nomeVacina());
    }

    @Test
    @DisplayName("Deve detectar pet com vacina vencida")
    void deveDetectarVacinaVencida() {
        Vacinacao vencida = entidadeSalva();
        vencida.setDataVencimento(LocalDate.now().minusDays(1));

        when(vacinacaoRepository.buscarVencidasDoPet(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of(vencida));

        assertTrue(vacinacaoService.petPossuiVacinaVencida(1L));
    }

    @Test
    @DisplayName("Deve remover com exclusao logica (sem delete fisico)")
    void deveRemoverComExclusaoLogica() {
        Vacinacao entity = entidadeSalva();

        when(vacinacaoRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(entity));
        when(vacinacaoRepository.save(entity)).thenReturn(entity);

        vacinacaoService.remover(1L);

        assertFalse(entity.getAtivo());
        verify(vacinacaoRepository).save(entity);
        verify(vacinacaoRepository, never()).delete(any());
    }
}
