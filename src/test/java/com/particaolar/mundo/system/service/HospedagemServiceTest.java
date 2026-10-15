package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.service.HospedagemService;
import com.particaolar.mundo.system.dto.request.HospedagemRequestDTO;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import com.particaolar.mundo.system.enums.Modalidade;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import com.particaolar.mundo.system.exception.CapacidadeExcedidaException;
import com.particaolar.mundo.system.exception.HospedagemNotFoundException;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.ReservaConflitanteException;
import com.particaolar.mundo.system.mapper.HospedagemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

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

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private com.particaolar.mundo.system.domain.repository.ServicoRepository servicoRepository;

    @InjectMocks
    private HospedagemService hospedagemService;

    private final LocalDateTime entrada = LocalDateTime.of(2026, 11, 3, 8, 0);
    private final LocalDateTime saida = LocalDateTime.of(2026, 11, 5, 18, 0);

    @BeforeEach
    @SuppressWarnings({"unchecked", "rawtypes"})
    void executarTransacaoImediatamente() {
        lenient().when(transactionTemplate.execute(any(TransactionCallback.class)))
                .thenAnswer((Answer<Object>) inv -> {
                    TransactionCallback<Object> cb = inv.getArgument(0);
                    return cb.doInTransaction(null);
                });
    }

    private Pet petAtivo() {
        Tutor tutor = new Tutor();
        tutor.setId(7L);
        tutor.setNome("Joao");
        Pet pet = new Pet();
        pet.setId(1L);
        pet.setNome("Rex");
        pet.setTutor(tutor);
        pet.setAtivo(true);
        return pet;
    }

    private HospedagemRequestDTO dto() {
        return new HospedagemRequestDTO(1L, Modalidade.HOTEL, entrada, saida, null);
    }

    private HospedagemResponseDTO responseDTO() {
        return new HospedagemResponseDTO(1L, 1L, "Rex", "Joao", Modalidade.HOTEL,
                entrada, saida, StatusHospedagem.AGENDADA, null, null, LocalDateTime.now());
    }

    @Test
    void deveSalvarHospedagemComSucesso() {
        Pet pet = petAtivo();
        Hospedagem hospedagem = new Hospedagem();
        Hospedagem hospedagemSalva = new Hospedagem();

        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(pet));
        when(hospedagemRepository.existeSobreposicao(eq(1L), any(), eq(entrada), eq(saida)))
                .thenReturn(false);
        when(hospedagemRepository.contarNoDia(eq(Modalidade.HOTEL), any(), any(), any()))
                .thenReturn(0L);
        when(hospedagemMapper.toEntity(dto())).thenReturn(hospedagem);
        when(hospedagemRepository.save(hospedagem)).thenReturn(hospedagemSalva);
        when(hospedagemMapper.toResponseDTO(hospedagemSalva)).thenReturn(responseDTO());

        HospedagemResponseDTO resultado = hospedagemService.salvar(dto());

        assertNotNull(resultado);
        assertEquals(StatusHospedagem.AGENDADA, resultado.status());
        verify(hospedagemRepository).save(hospedagem);
    }

    @Test
    void deveLancarExcecaoQuandoPetNaoEncontradoAoSalvar() {
        when(petRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

        assertThrows(PetNotFoundException.class,
                () -> hospedagemService.salvar(
                        new HospedagemRequestDTO(99L, Modalidade.HOTEL, entrada, saida, null)));
    }

    @Test
    void deveRejeitarSobreposicaoDoMesmoPet() {
        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petAtivo()));
        when(hospedagemRepository.existeSobreposicao(eq(1L), any(), eq(entrada), eq(saida)))
                .thenReturn(true);

        assertThrows(ReservaConflitanteException.class,
                () -> hospedagemService.salvar(dto()));
        verify(hospedagemRepository, never()).save(any());
    }

    @Test
    void deveRejeitarQuandoCapacidadeEsgotada() {
        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petAtivo()));
        when(hospedagemRepository.existeSobreposicao(eq(1L), any(), eq(entrada), eq(saida)))
                .thenReturn(false);
        when(hospedagemRepository.contarNoDia(eq(Modalidade.HOTEL), any(), any(), any()))
                .thenReturn(20L);

        assertThrows(CapacidadeExcedidaException.class,
                () -> hospedagemService.salvar(dto()));
        verify(hospedagemRepository, never()).save(any());
    }

    @Test
    void devePermitirReservasAdjacentes() {
        // Adjacencia nao eh sobreposicao: o repositorio retorna false quando
        // nova entrada == saida existente ([entrada, saida) semifechado).
        // Aqui apenas garantimos que salvar delega ao repositorio e persiste.
        when(petRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(petAtivo()));
        when(hospedagemRepository.existeSobreposicao(eq(1L), any(), eq(entrada), eq(saida)))
                .thenReturn(false);
        when(hospedagemRepository.contarNoDia(eq(Modalidade.HOTEL), any(), any(), any()))
                .thenReturn(0L);
        Hospedagem hospedagem = new Hospedagem();
        when(hospedagemMapper.toEntity(dto())).thenReturn(hospedagem);
        when(hospedagemRepository.save(hospedagem)).thenReturn(hospedagem);
        when(hospedagemMapper.toResponseDTO(hospedagem)).thenReturn(responseDTO());

        assertNotNull(hospedagemService.salvar(dto()));
        verify(hospedagemRepository).save(hospedagem);
    }

    @Test
    void deveLancarExcecaoComDatasInvalidas() {
        HospedagemRequestDTO dtoInvalido = new HospedagemRequestDTO(1L, Modalidade.HOTEL,
                LocalDateTime.of(2026, 11, 5, 18, 0),
                LocalDateTime.of(2026, 11, 3, 8, 0), null);

        assertThrows(IllegalArgumentException.class,
                () -> hospedagemService.salvar(dtoInvalido));
    }



    @Test
    void deveFazerCheckInECheckOutEmSequencia() {
        Hospedagem hospedagem = new Hospedagem();
        hospedagem.setStatus(StatusHospedagem.AGENDADA);
        when(hospedagemRepository.findById(1L)).thenReturn(Optional.of(hospedagem));
        when(hospedagemMapper.toResponseDTO(hospedagem)).thenReturn(responseDTO());

        hospedagemService.checkIn(1L);
        assertEquals(StatusHospedagem.EM_ANDAMENTO, hospedagem.getStatus());

        hospedagemService.checkOut(1L);
        assertEquals(StatusHospedagem.CONCLUIDA, hospedagem.getStatus());
    }

    @Test
    void deveRejeitarCheckInDuplicadoECheckOutSemCheckIn() {
        Hospedagem emAndamento = new Hospedagem();
        emAndamento.setStatus(StatusHospedagem.EM_ANDAMENTO);
        when(hospedagemRepository.findById(1L)).thenReturn(Optional.of(emAndamento));
        assertThrows(IllegalStateException.class, () -> hospedagemService.checkIn(1L));

        Hospedagem agendada = new Hospedagem();
        agendada.setStatus(StatusHospedagem.AGENDADA);
        when(hospedagemRepository.findById(2L)).thenReturn(Optional.of(agendada));
        assertThrows(IllegalStateException.class, () -> hospedagemService.checkOut(2L));
    }

    @Test
    void deveRejeitarReagendamentoSobreposto() {
        Hospedagem existente = new Hospedagem();
        existente.setId(5L);
        existente.setStatus(StatusHospedagem.AGENDADA);
        existente.setPet(petAtivo());
        when(hospedagemRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(hospedagemRepository.existeSobreposicaoExceto(
                eq(1L), any(), eq(entrada), eq(saida), eq(5L))).thenReturn(true);

        assertThrows(ReservaConflitanteException.class,
                () -> hospedagemService.atualizar(5L, dto()));
    }

    @Test
    void deveLancarExcecaoAoAtualizarHospedagemCancelada() {
        Hospedagem hospedagem = new Hospedagem();
        hospedagem.setStatus(StatusHospedagem.CANCELADA);

        when(hospedagemRepository.findById(1L)).thenReturn(Optional.of(hospedagem));

        assertThrows(IllegalStateException.class,
                () -> hospedagemService.atualizar(1L, dto()));
    }

    @Test
    void deveCancelarHospedagemComSucesso() {
        Hospedagem hospedagem = new Hospedagem();
        hospedagem.setStatus(StatusHospedagem.AGENDADA);

        when(hospedagemRepository.findById(1L)).thenReturn(Optional.of(hospedagem));

        hospedagemService.cancelar(1L, "Tutor desistiu");

        assertEquals(StatusHospedagem.CANCELADA, hospedagem.getStatus());
    }

    @Test
    void deveListarComPaginacao() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Hospedagem> pagina = new PageImpl<>(List.of(new Hospedagem()));
        when(hospedagemRepository.findAll(pageable)).thenReturn(pagina);
        when(hospedagemMapper.toResponseDTO(any(Hospedagem.class))).thenReturn(responseDTO());

        Page<HospedagemResponseDTO> resultado = hospedagemService.listarTodas(pageable);

        assertEquals(1, resultado.getTotalElements());
    }

    @Test
    void deveLancarExcecaoComPaginaMuitoGrande() {
        Pageable pageable = PageRequest.of(0, 101);

        assertThrows(IllegalArgumentException.class,
                () -> hospedagemService.listarTodas(pageable));
    }
}
