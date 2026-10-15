package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.ServicoRepository;
import com.particaolar.mundo.system.dto.request.HospedagemRequestDTO;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import com.particaolar.mundo.system.enums.Modalidade;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import com.particaolar.mundo.system.exception.CapacidadeExcedidaException;
import com.particaolar.mundo.system.exception.HospedagemNotFoundException;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.ReservaConflitanteException;
import com.particaolar.mundo.system.mapper.HospedagemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospedagemService {

    private final HospedagemRepository hospedagemRepository;
    private final PetRepository petRepository;
    private final HospedagemMapper hospedagemMapper;
    private final TransactionTemplate transactionTemplate;
    private final ServicoRepository servicoRepository;
    private final Object reservaLock = new Object();

    @Value("${app.creche.capacidade.creche:10}")
    private int capacidadeCreche = 10;

    @Value("${app.creche.capacidade.hotel:20}")
    private int capacidadeHotel = 20;

    // Valid status transitions
    private static final Set<StatusHospedagem> ACTIVE_STATUSES =
            Set.of(StatusHospedagem.AGENDADA, StatusHospedagem.EM_ANDAMENTO);

    public HospedagemResponseDTO salvar(HospedagemRequestDTO dto) {
        validarIntervalo(dto.dataEntrada(), dto.dataSaida());
        synchronized (reservaLock) {
            return transactionTemplate.execute(tx -> {
                Pet pet = petRepository.findByIdAndAtivoTrue(dto.petId())
                        .orElseThrow(() -> new PetNotFoundException(dto.petId()));
                verificarSobreposicao(dto.petId(), null, dto.dataEntrada(), dto.dataSaida());
                verificarCapacidade(null, dto.modalidade(), dto.dataEntrada(), dto.dataSaida());
                Hospedagem hospedagem = hospedagemMapper.toEntity(dto);
                hospedagem.setPet(pet);
                if (dto.servicoId() != null && servicoRepository != null) {
                    servicoRepository.findById(dto.servicoId()).ifPresent(s -> {
                        hospedagem.setServico(s);
                        if (hospedagem.getPrecoAcordado() == null) {
                            hospedagem.setPrecoAcordado(s.getPreco());
                        }
                    });
                }
                Hospedagem salva = hospedagemRepository.save(hospedagem);
                log.info("Hospedagem criada petId={}", dto.petId());
                return hospedagemMapper.toResponseDTO(salva);
            });
        }
    }

    @Transactional(readOnly = true)
    public Page<HospedagemResponseDTO> listarTodas(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return hospedagemRepository.findAll(pageable)
                .map(hospedagemMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public HospedagemResponseDTO buscarPorId(Long id) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    @Transactional(readOnly = true)
    public List<HospedagemResponseDTO> buscarPorPetId(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new PetNotFoundException(petId);
        }
        return hospedagemRepository.findByPetId(petId)
                .stream()
                .map(hospedagemMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public HospedagemResponseDTO atualizarStatus(Long id, StatusHospedagem novoStatus) {
        return atualizarStatus(id, novoStatus, null);
    }

    @Transactional
    public HospedagemResponseDTO atualizarStatus(Long id, StatusHospedagem novoStatus, String motivo) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));

        validateStatusTransition(hospedagem.getStatus(), novoStatus);
        if (novoStatus == StatusHospedagem.CANCELADA || novoStatus == StatusHospedagem.FALTA) {
            hospedagem.setMotivoCancelamento(validarMotivo(motivo));
        }
        hospedagem.setStatus(novoStatus);
        log.info("Status da hospedagem {} atualizado para {}", id, novoStatus);
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    public HospedagemResponseDTO atualizar(Long id, HospedagemRequestDTO dto) {
        validarIntervalo(dto.dataEntrada(), dto.dataSaida());
        synchronized (reservaLock) {
            return transactionTemplate.execute(tx -> {
                Hospedagem hospedagem = hospedagemRepository.findById(id)
                        .orElseThrow(() -> new HospedagemNotFoundException(id));
                if (hospedagem.getStatus() == StatusHospedagem.CANCELADA
                        || hospedagem.getStatus() == StatusHospedagem.CONCLUIDA) {
                    throw new IllegalStateException(
                            "Nao e possivel reagendar uma hospedagem " + hospedagem.getStatus());
                }
                verificarSobreposicao(hospedagem.getPet().getId(), id,
                        dto.dataEntrada(), dto.dataSaida());
                verificarCapacidade(id, dto.modalidade(), dto.dataEntrada(), dto.dataSaida());
                hospedagemMapper.updateEntityFromDTO(dto, hospedagem);
                log.info("Hospedagem {} reagendada", id);
                return hospedagemMapper.toResponseDTO(hospedagem);
            });
        }
    }

    @Transactional
    public void cancelar(Long id, String motivo) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));

        if (hospedagem.getStatus() == StatusHospedagem.CANCELADA) {
            throw new IllegalStateException("Hospedagem ja esta cancelada");
        }
        if (hospedagem.getStatus() == StatusHospedagem.CONCLUIDA
                || hospedagem.getStatus() == StatusHospedagem.FALTA) {
            throw new IllegalStateException("Nao e possivel cancelar uma hospedagem "
                    + hospedagem.getStatus());
        }

        hospedagem.setMotivoCancelamento(validarMotivo(motivo));
        hospedagem.setStatus(StatusHospedagem.CANCELADA);
        log.info("Hospedagem {} cancelada", id);
    }

    @Transactional
    public HospedagemResponseDTO marcarFalta(Long id, String motivo) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));
        if (hospedagem.getStatus() != StatusHospedagem.AGENDADA
                && hospedagem.getStatus() != StatusHospedagem.EM_ANDAMENTO) {
            throw new IllegalStateException("Falta exige status AGENDADA ou EM_ANDAMENTO (atual: "
                    + hospedagem.getStatus() + ")");
        }
        hospedagem.setMotivoCancelamento(validarMotivo(motivo));
        hospedagem.setStatus(StatusHospedagem.FALTA);
        log.info("Falta registrada na hospedagem {}", id);
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    @Transactional
    public HospedagemResponseDTO checkIn(Long id) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));
        if (hospedagem.getStatus() != StatusHospedagem.AGENDADA) {
            throw new IllegalStateException("Check-in exige status AGENDADA (atual: "
                    + hospedagem.getStatus() + ")");
        }
        hospedagem.setStatus(StatusHospedagem.EM_ANDAMENTO);
        hospedagem.setCheckInEm(LocalDateTime.now());
        log.info("Check-in hospedagem {}", id);
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    @Transactional
    public HospedagemResponseDTO checkOut(Long id) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));
        if (hospedagem.getStatus() != StatusHospedagem.EM_ANDAMENTO) {
            throw new IllegalStateException("Check-out exige status EM_ANDAMENTO (atual: "
                    + hospedagem.getStatus() + ")");
        }
        hospedagem.setStatus(StatusHospedagem.CONCLUIDA);
        hospedagem.setCheckOutEm(LocalDateTime.now());
        log.info("Check-out hospedagem {}", id);
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    private void validateStatusTransition(StatusHospedagem current, StatusHospedagem next) {
        if (current == next) return;

        boolean valid = switch (current) {
            case AGENDADA -> next == StatusHospedagem.EM_ANDAMENTO
                    || next == StatusHospedagem.CANCELADA;
            case EM_ANDAMENTO -> next == StatusHospedagem.CONCLUIDA
                    || next == StatusHospedagem.CANCELADA;
            case CONCLUIDA, CANCELADA, FALTA -> false;
        };

        if (!valid) {
            throw new IllegalStateException("Transicao invalida: " + current + " -> " + next);
        }
    }

    private String validarMotivo(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("O motivo e obrigatorio para cancelamento ou falta");
        }
        if (motivo.length() > 500) {
            throw new IllegalArgumentException("O motivo deve ter no maximo 500 caracteres");
        }
        return motivo.strip();
    }

    private void validarIntervalo(LocalDateTime entrada, LocalDateTime saida) {
        if (entrada == null || saida == null || !saida.isAfter(entrada)) {
            throw new IllegalArgumentException("A data de saida deve ser posterior a data de entrada");
        }
    }

    private void verificarSobreposicao(Long petId, Long excluindoId,
                                       LocalDateTime entrada, LocalDateTime saida) {
        boolean conflito = (excluindoId == null)
                ? hospedagemRepository.existeSobreposicao(petId, ACTIVE_STATUSES, entrada, saida)
                : hospedagemRepository.existeSobreposicaoExceto(petId, ACTIVE_STATUSES,
                        entrada, saida, excluindoId);
        if (conflito) {
            throw new ReservaConflitanteException(petId);
        }
    }

    private void verificarCapacidade(Long excluindoId, Modalidade modalidade,
                                     LocalDateTime entrada, LocalDateTime saida) {
        int capacidade = (modalidade == Modalidade.CRECHE) ? capacidadeCreche : capacidadeHotel;
        LocalDate dia = entrada.toLocalDate();
        LocalDate fim = saida.toLocalDate();
        while (!dia.isAfter(fim)) {
            LocalDateTime inicioDia = dia.atStartOfDay();
            LocalDateTime inicioProximo = dia.plusDays(1).atStartOfDay();
            long ocupadas = (excluindoId == null)
                    ? hospedagemRepository.contarNoDia(modalidade, ACTIVE_STATUSES,
                            inicioDia, inicioProximo)
                    : hospedagemRepository.contarNoDiaExceto(modalidade, ACTIVE_STATUSES,
                            inicioDia, inicioProximo, excluindoId);
            if (ocupadas >= capacidade) {
                throw new CapacidadeExcedidaException(dia, modalidade);
            }
            dia = dia.plusDays(1);
        }
    }
}