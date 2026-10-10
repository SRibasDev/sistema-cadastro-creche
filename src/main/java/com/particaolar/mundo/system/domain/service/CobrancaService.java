package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Cobranca;
import com.particaolar.mundo.system.domain.entity.Desconto;
import com.particaolar.mundo.system.domain.entity.PacoteComprado;
import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.CobrancaRepository;
import com.particaolar.mundo.system.domain.repository.DescontoRepository;
import com.particaolar.mundo.system.domain.repository.PacoteCompradoRepository;
import com.particaolar.mundo.system.domain.repository.ReservaRepository;
import com.particaolar.mundo.system.dto.request.CobrancaRequestDTO;
import com.particaolar.mundo.system.dto.response.CobrancaResponseDTO;
import com.particaolar.mundo.system.enums.OrigemCobranca;
import com.particaolar.mundo.system.enums.StatusCobranca;
import com.particaolar.mundo.system.enums.TipoDesconto;
import com.particaolar.mundo.system.exception.CobrancaNotFoundException;
import com.particaolar.mundo.system.mapper.CobrancaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Cobranças: geradas por reserva ou pacote, com vencimento, status
 * (pendente, paga, atrasada, cancelada) e pagamento manual.
 * O job de atraso (atualizarAtrasadas) roda diariamente (Etapa 6).
 * SaaS: filtros futuros por tenant_id.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final ReservaRepository reservaRepository;
    private final PacoteCompradoRepository pacoteCompradoRepository;
    private final DescontoRepository descontoRepository;
    private final CobrancaMapper cobrancaMapper;
    private final Clock clock;

    @Transactional
    public CobrancaResponseDTO salvar(CobrancaRequestDTO dto) {
        Pet pet;
        Tutor tutor;
        Reserva reserva = null;
        PacoteComprado pacoteComprado = null;

        if (dto.origem() == OrigemCobranca.RESERVA) {
            reserva = reservaRepository.findById(dto.reservaId())
                    .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada"));
            pet = reserva.getPet();
            tutor = pet.getTutor();
        } else {
            pacoteComprado = pacoteCompradoRepository.findById(dto.pacoteCompradoId())
                    .orElseThrow(() -> new IllegalArgumentException("Pacote comprado não encontrado"));
            pet = pacoteComprado.getPet();
            tutor = pet.getTutor();
        }

        Desconto desconto = dto.descontoId() != null
                ? descontoRepository.findById(dto.descontoId()).orElse(null)
                : null;

        Cobranca cobranca = new Cobranca();
        cobranca.setOrigem(dto.origem());
        cobranca.setReserva(reserva);
        cobranca.setPacoteComprado(pacoteComprado);
        cobranca.setPet(pet);
        cobranca.setTutor(tutor);
        cobranca.setDescricao(dto.descricao());
        cobranca.setValor(dto.valor());
        cobranca.setDesconto(desconto);
        cobranca.setValorFinal(aplicarDesconto(dto.valor(), desconto));
        cobranca.setDataVencimento(dto.dataVencimento());
        cobranca.setObservacoes(dto.observacoes());
        cobranca.setStatus(StatusCobranca.PENDENTE);

        Cobranca salva = cobrancaRepository.save(cobranca);
        log.info("Cobrança {} criada ({}): R$ {} vencendo {}",
                salva.getId(), dto.origem(), salva.getValorFinal(), dto.dataVencimento());
        return cobrancaMapper.toResponseDTO(salva);
    }
    @Transactional(readOnly = true)
    public Page<CobrancaResponseDTO> listarTodas(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return cobrancaRepository.findAll(pageable).map(cobrancaMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public CobrancaResponseDTO buscarPorId(Long id) {
        return cobrancaMapper.toResponseDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<CobrancaResponseDTO> listarPorStatus(StatusCobranca status, Pageable pageable) {
        return cobrancaRepository.findByStatus(status, pageable).map(cobrancaMapper::toResponseDTO);
    }

    /** Registra pagamento manual. */
    @Transactional
    public CobrancaResponseDTO registrarPagamento(Long id, LocalDate dataPagamento) {
        Cobranca cobranca = buscarEntidade(id);
        if (cobranca.getStatus() == StatusCobranca.PAGA) {
            throw new IllegalStateException("Cobrança já está paga");
        }
        if (cobranca.getStatus() == StatusCobranca.CANCELADA) {
            throw new IllegalStateException("Não é possível pagar uma cobrança cancelada");
        }
        cobranca.setStatus(StatusCobranca.PAGA);
        cobranca.setDataPagamento(dataPagamento != null ? dataPagamento : LocalDate.now(clock));
        log.info("Pagamento registrado na cobrança {} em {}", id, cobranca.getDataPagamento());
        return cobrancaMapper.toResponseDTO(cobranca);
    }

    @Transactional
    public void cancelar(Long id, String motivo) {
        Cobranca cobranca = buscarEntidade(id);
        if (cobranca.getStatus() == StatusCobranca.PAGA) {
            throw new IllegalStateException("Não é possível cancelar uma cobrança paga");
        }
        cobranca.setStatus(StatusCobranca.CANCELADA);
        cobranca.setObservacoes(motivo);
        log.info("Cobrança {} cancelada: {}", id, motivo);
    }

    /**
     * Marca como ATRASADA as cobranças PENDENTE com vencimento anterior a hoje.
     * Executada pelo job diário (Etapa 6). Retorna quantas foram atualizadas.
     */
    @Transactional
    public int atualizarAtrasadas() {
        LocalDate hoje = LocalDate.now(clock);
        List<Cobranca> vencidas = cobrancaRepository.findByStatusAndDataVencimentoLessThan(
                StatusCobranca.PENDENTE, hoje);
        for (Cobranca c : vencidas) {
            c.setStatus(StatusCobranca.ATRASADA);
        }
        if (!vencidas.isEmpty()) {
            log.info("{} cobrança(s) marcada(s) como ATRASADA", vencidas.size());
        }
        return vencidas.size();
    }

    /** Cobranças com um vencimento específico (usado pelo job de e-mail). */
    @Transactional(readOnly = true)
    public List<CobrancaResponseDTO> buscarPorVencimento(LocalDate data) {
        return cobrancaRepository.buscarPorStatusEVencimento(StatusCobranca.PENDENTE, data).stream()
                .map(cobrancaMapper::toResponseDTO)
                .toList();
    }

    Cobranca buscarEntidade(Long id) {
        return cobrancaRepository.findById(id)
                .orElseThrow(() -> new CobrancaNotFoundException(id));
    }

    private BigDecimal aplicarDesconto(BigDecimal valor, Desconto desconto) {
        if (desconto == null) {
            return valor;
        }
        if (desconto.getTipo() == TipoDesconto.PERCENTUAL) {
            BigDecimal fator = BigDecimal.ONE.subtract(
                    desconto.getValor().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            return valor.multiply(fator).setScale(2, RoundingMode.HALF_UP);
        }
        return valor.subtract(desconto.getValor()).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }
}
