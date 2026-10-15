package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Cobranca;
import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.domain.entity.Pagamento;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.CobrancaRepository;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.repository.PagamentoRepository;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.dto.request.CobrancaRequestDTO;
import com.particaolar.mundo.system.dto.request.PagamentoRequestDTO;
import com.particaolar.mundo.system.dto.response.CobrancaResponseDTO;
import com.particaolar.mundo.system.dto.response.PagamentoResponseDTO;
import com.particaolar.mundo.system.enums.StatusCobranca;
import com.particaolar.mundo.system.exception.CobrancaNotFoundException;
import com.particaolar.mundo.system.exception.HospedagemNotFoundException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.mapper.CobrancaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CobrancaService {

    private final CobrancaRepository cobrancaRepository;
    private final PagamentoRepository pagamentoRepository;
    private final TutorRepository tutorRepository;
    private final HospedagemRepository hospedagemRepository;
    private final CobrancaMapper cobrancaMapper;
    private final AuditLogService auditLogService;
    private final Object financeiroLock = new Object();

    @Transactional
    public CobrancaResponseDTO criar(CobrancaRequestDTO dto, String usuario) {
        Tutor tutor = tutorRepository.findByIdAndAtivoTrue(dto.tutorId())
                .orElseThrow(() -> new TutorNotFoundException(dto.tutorId()));

        Hospedagem hospedagem = null;
        if (dto.hospedagemId() != null) {
            hospedagem = hospedagemRepository.findById(dto.hospedagemId())
                    .orElseThrow(() -> new HospedagemNotFoundException(dto.hospedagemId()));
        }

        BigDecimal desconto = dto.desconto() != null ? dto.desconto() : BigDecimal.ZERO;
        if (desconto.compareTo(dto.valorTotal()) > 0) {
            throw new IllegalArgumentException("O desconto não pode ser maior que o valor total da cobrança.");
        }

        Cobranca cobranca = cobrancaMapper.toEntity(dto);
        cobranca.setTutor(tutor);
        cobranca.setHospedagem(hospedagem);
        cobranca.setDesconto(desconto);
        cobranca.setValorCobrado(dto.valorTotal().subtract(desconto));
        cobranca.setValorPago(BigDecimal.ZERO);
        cobranca.setStatus(StatusCobranca.PENDENTE);

        Cobranca salva = cobrancaRepository.save(cobranca);
        log.info("Cobrança criada ID={} tutorId={} valorCobrado={}", salva.getId(), tutor.getId(), salva.getValorCobrado());
        auditLogService.registrar(usuario, "CRIAR_COBRANCA", "COBRANCA", salva.getId(),
                "Valor: R$ " + salva.getValorCobrado() + " Tutor: " + tutor.getNome());

        return cobrancaMapper.toResponseDTO(salva);
    }

    public PagamentoResponseDTO registrarPagamento(PagamentoRequestDTO dto, String usuario) {
        synchronized (financeiroLock) {
            return executarPagamento(dto, usuario);
        }
    }

    @Transactional
    protected PagamentoResponseDTO executarPagamento(PagamentoRequestDTO dto, String usuario) {
        Cobranca cobranca = cobrancaRepository.findById(dto.cobrancaId())
                .orElseThrow(() -> new CobrancaNotFoundException(dto.cobrancaId()));

        if (cobranca.getStatus() == StatusCobranca.CANCELADA) {
            throw new IllegalStateException("Não é permitido registrar pagamento em cobrança cancelada.");
        }
        if (cobranca.getStatus() == StatusCobranca.PAGA) {
            throw new IllegalStateException("Esta cobrança já está quitada.");
        }

        BigDecimal saldoDevedor = cobranca.getSaldoDevedor();
        if (dto.valor().compareTo(saldoDevedor) > 0) {
            throw new IllegalArgumentException(String.format(
                    "O valor do pagamento (R$ %.2f) excede o saldo devedor restante (R$ %.2f).",
                    dto.valor(), saldoDevedor));
        }

        Pagamento pagamento = new Pagamento();
        pagamento.setCobranca(cobranca);
        pagamento.setValor(dto.valor());
        pagamento.setDataPagamento(LocalDateTime.now());
        pagamento.setFormaPagamento(dto.formaPagamento());
        pagamento.setComprovante(dto.comprovante() != null ? dto.comprovante().trim() : null);
        pagamento.setObservacoes(dto.observacoes() != null ? dto.observacoes().trim() : null);
        pagamento.setRegistradoPor(usuario != null ? usuario : "SISTEMA");

        Pagamento salvo = pagamentoRepository.save(pagamento);

        BigDecimal novoValorPago = cobranca.getValorPago().add(dto.valor());
        cobranca.setValorPago(novoValorPago);

        if (novoValorPago.compareTo(cobranca.getValorCobrado()) >= 0) {
            cobranca.setStatus(StatusCobranca.PAGA);
        } else {
            cobranca.setStatus(StatusCobranca.PAGA_PARCIAL);
        }

        cobrancaRepository.save(cobranca);
        log.info("Pagamento registrado ID={} cobrançaID={} valor={}", salvo.getId(), cobranca.getId(), dto.valor());
        auditLogService.registrar(usuario, "REGISTRAR_PAGAMENTO", "PAGAMENTO", salvo.getId(),
                "Valor: R$ " + dto.valor() + " CobrancaID: " + cobranca.getId());

        return cobrancaMapper.toPagamentoResponseDTO(salvo);
    }

    @Transactional
    public void cancelar(Long id, String motivo, String usuario) {
        Cobranca cobranca = cobrancaRepository.findById(id)
                .orElseThrow(() -> new CobrancaNotFoundException(id));

        if (cobranca.getStatus() == StatusCobranca.CANCELADA) {
            throw new IllegalStateException("Cobrança já está cancelada.");
        }
        if (cobranca.getValorPago().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Não é possível cancelar uma cobrança com pagamentos já registrados.");
        }

        cobranca.setStatus(StatusCobranca.CANCELADA);
        if (motivo != null && !motivo.isBlank()) {
            String obsAtual = cobranca.getObservacoes() != null ? cobranca.getObservacoes() + " | " : "";
            cobranca.setObservacoes(obsAtual + "Cancelado: " + motivo.trim());
        }
        cobrancaRepository.save(cobranca);
        log.info("Cobrança ID={} cancelada", id);
        auditLogService.registrar(usuario, "CANCELAR_COBRANCA", "COBRANCA", id, "Motivo: " + motivo);
    }

    @Transactional(readOnly = true)
    public Page<CobrancaResponseDTO> listar(Pageable pageable, StatusCobranca status) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        if (status != null) {
            return cobrancaRepository.findByStatus(status, pageable)
                    .map(cobrancaMapper::toResponseDTO);
        }
        return cobrancaRepository.findAll(pageable)
                .map(cobrancaMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public CobrancaResponseDTO buscarPorId(Long id) {
        Cobranca cobranca = cobrancaRepository.findById(id)
                .orElseThrow(() -> new CobrancaNotFoundException(id));
        return cobrancaMapper.toResponseDTO(cobranca);
    }

    @Transactional(readOnly = true)
    public List<CobrancaResponseDTO> listarPorTutor(Long tutorId) {
        return cobrancaRepository.findByTutorId(tutorId).stream()
                .map(cobrancaMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CobrancaResponseDTO> listarPorHospedagem(Long hospedagemId) {
        return cobrancaRepository.findByHospedagemId(hospedagemId).stream()
                .map(cobrancaMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal obterTotalSaldoDevedor() {
        return cobrancaRepository.somarSaldoDevedorPorStatus(
                List.of(StatusCobranca.PENDENTE, StatusCobranca.PAGA_PARCIAL)
        );
    }

    @Transactional(readOnly = true)
    public long obterQuantidadePendentes() {
        return cobrancaRepository.countByStatusIn(
                List.of(StatusCobranca.PENDENTE, StatusCobranca.PAGA_PARCIAL)
        );
    }
}
