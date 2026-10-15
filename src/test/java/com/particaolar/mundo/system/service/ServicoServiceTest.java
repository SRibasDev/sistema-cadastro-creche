package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Servico;
import com.particaolar.mundo.system.domain.repository.ServicoRepository;
import com.particaolar.mundo.system.domain.service.ServicoService;
import com.particaolar.mundo.system.dto.request.ServicoRequestDTO;
import com.particaolar.mundo.system.dto.response.ServicoResponseDTO;
import com.particaolar.mundo.system.enums.TipoServico;
import com.particaolar.mundo.system.enums.UnidadeCobranca;
import com.particaolar.mundo.system.exception.ServicoNotFoundException;
import com.particaolar.mundo.system.mapper.ServicoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private ServicoMapper servicoMapper;

    @InjectMocks
    private ServicoService servicoService;

    private Servico servico;
    private ServicoRequestDTO requestDTO;
    private ServicoResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        servico = new Servico();
        servico.setId(1L);
        servico.setNome("Diária Creche");
        servico.setDescricao("Creche das 8h às 18h");
        servico.setTipo(TipoServico.CRECHE);
        servico.setPreco(new BigDecimal("70.00"));
        servico.setUnidade(UnidadeCobranca.DIARIA);
        servico.setAtivo(true);

        requestDTO = new ServicoRequestDTO(
                "Diária Creche",
                "Creche das 8h às 18h",
                TipoServico.CRECHE,
                new BigDecimal("70.00"),
                UnidadeCobranca.DIARIA
        );

        responseDTO = new ServicoResponseDTO(
                1L,
                "Diária Creche",
                "Creche das 8h às 18h",
                TipoServico.CRECHE,
                new BigDecimal("70.00"),
                UnidadeCobranca.DIARIA,
                true,
                null,
                null
        );
    }

    @Test
    @DisplayName("Deve salvar serviço com sucesso")
    void deveSalvarServico() {
        when(servicoRepository.existsByNomeIgnoreCase("Diária Creche")).thenReturn(false);
        when(servicoMapper.toEntity(requestDTO)).thenReturn(servico);
        when(servicoRepository.save(servico)).thenReturn(servico);
        when(servicoMapper.toResponseDTO(servico)).thenReturn(responseDTO);

        ServicoResponseDTO res = servicoService.salvar(requestDTO);

        assertNotNull(res);
        assertEquals(1L, res.id());
        assertEquals("Diária Creche", res.nome());
        verify(servicoRepository).save(servico);
    }

    @Test
    @DisplayName("Deve impedir serviço com nome duplicado")
    void deveImpedirNomeDuplicado() {
        when(servicoRepository.existsByNomeIgnoreCase("Diária Creche")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> servicoService.salvar(requestDTO));
        verify(servicoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar serviços ativos")
    void deveListarAtivos() {
        when(servicoRepository.findByAtivoTrue(any(PageRequest.class)))
                .thenReturn(new PageImpl<>(List.of(servico)));
        when(servicoMapper.toResponseDTO(servico)).thenReturn(responseDTO);

        Page<ServicoResponseDTO> pagina = servicoService.listarAtivos(PageRequest.of(0, 10));

        assertNotNull(pagina);
        assertEquals(1, pagina.getTotalElements());
    }

    @Test
    @DisplayName("Deve buscar serviço por ID")
    void deveBuscarPorId() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoMapper.toResponseDTO(servico)).thenReturn(responseDTO);

        ServicoResponseDTO res = servicoService.buscarPorId(1L);

        assertEquals(1L, res.id());
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar serviço inexistente")
    void deveLancarExceptionIdInexistente() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ServicoNotFoundException.class, () -> servicoService.buscarPorId(99L));
    }

    @Test
    @DisplayName("Deve atualizar serviço com sucesso")
    void deveAtualizarServico() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.existsByNomeIgnoreCaseAndIdNot("Diária Creche", 1L)).thenReturn(false);
        when(servicoRepository.save(servico)).thenReturn(servico);
        when(servicoMapper.toResponseDTO(servico)).thenReturn(responseDTO);

        ServicoResponseDTO res = servicoService.atualizar(1L, requestDTO);

        assertNotNull(res);
        verify(servicoMapper).updateEntityFromDTO(requestDTO, servico);
    }

    @Test
    @DisplayName("Deve desativar serviço com sucesso")
    void deveDesativarServico() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));

        servicoService.alterarStatus(1L, false);

        assertFalse(servico.getAtivo());
        verify(servicoRepository).save(servico);
    }
}
