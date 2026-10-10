package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.domain.repository.ReservaRepository;
import com.particaolar.mundo.system.dto.response.ReservaResponseDTO;
import com.particaolar.mundo.system.enums.StatusReserva;
import com.particaolar.mundo.system.enums.TipoReserva;
import com.particaolar.mundo.system.exception.IllegalPresenceStateException;
import com.particaolar.mundo.system.exception.ReservaNotFoundException;
import com.particaolar.mundo.system.mapper.ReservaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Presença do pet: o FUNCIONARIO marca entrada (EM_ANDAMENTO), saída
 * (CONCLUIDA) e falta (FALTOU). Nenhuma escrita em cadastros.
 * Na entrada o saldo do pacote do pet é debitado automaticamente
 * (PacoteService.debitarSaldo).
 * SaaS: validações receberão tenant_id no futuro.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PresencaService {

    private final ReservaRepository reservaRepository;
    private final ReservaMapper reservaMapper;
    private final PacoteService pacoteService;

    /** Marca entrada de um pet em reserva AGENDADA. */
    @Transactional
    public ReservaResponseDTO marcarEntrada(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        if (reserva.getStatus() != StatusReserva.AGENDADA) {
            throw new IllegalPresenceStateException(
                    "Só é possível marcar entrada em reserva AGENDADA (atual: " + reserva.getStatus() + ")");
        }
        reserva.setStatus(StatusReserva.EM_ANDAMENTO);
        // Débito automático de saldo do pacote (se o pet tiver saldo ativo)
        Long pacoteConsumido = pacoteService.debitarSaldo(reserva.getPet().getId(), reserva);
        if (pacoteConsumido != null) {
            log.info("Entrada marcada: reserva={} pet={} pacote debitado={}",
                    reservaId, reserva.getPet().getNome(), pacoteConsumido);
        } else {
            log.info("Entrada marcada: reserva={} pet={} (sem saldo de pacote)",
                    reservaId, reserva.getPet().getNome());
        }
        return reservaMapper.toResponseDTO(reserva);
    }

    /** Marca saída de um pet em reserva EM_ANDAMENTO. */
    @Transactional
    public ReservaResponseDTO marcarSaida(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        if (reserva.getStatus() != StatusReserva.EM_ANDAMENTO) {
            throw new IllegalPresenceStateException(
                    "Só é possível marcar saída em reserva EM_ANDAMENTO (atual: " + reserva.getStatus() + ")");
        }
        reserva.setStatus(StatusReserva.CONCLUIDA);
        log.info("Saída marcada: reserva={} pet={}", reservaId, reserva.getPet().getNome());
        return reservaMapper.toResponseDTO(reserva);
    }

    /** Marca falta em reserva AGENDADA. */
    @Transactional
    public ReservaResponseDTO marcarFalta(Long reservaId) {
        Reserva reserva = reservaRepository.findById(reservaId)
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));
        if (reserva.getStatus() != StatusReserva.AGENDADA) {
            throw new IllegalPresenceStateException(
                    "Só é possível marcar falta em reserva AGENDADA (atual: " + reserva.getStatus() + ")");
        }
        reserva.setStatus(StatusReserva.FALTOU);
        log.info("Falta marcada: reserva={} pet={}", reservaId, reserva.getPet().getNome());
        return reservaMapper.toResponseDTO(reserva);
    }

    /**
     * Marca entrada de TODAS as reservas AGENDADA do dia.
     * Retorna as afetadas.
     */
    @Transactional
    public List<ReservaResponseDTO> marcarTodosPresentes(LocalDate dia) {
        List<StatusReserva> ativos = List.of(StatusReserva.AGENDADA);
        List<Reserva> agendadas = reservaRepository
                .findByDataEntradaLessThanEqualAndDataSaidaGreaterThanEqualAndStatusInOrderByDataEntradaAscHoraEntradaAsc(
                        dia, dia, ativos);
        List<ReservaResponseDTO> marcadas = new ArrayList<>();
        for (Reserva reserva : agendadas) {
            reserva.setStatus(StatusReserva.EM_ANDAMENTO);
            pacoteService.debitarSaldo(reserva.getPet().getId(), reserva);
            marcadas.add(reservaMapper.toResponseDTO(reserva));
        }
        log.info("Marcadas {} entradas no dia {}", marcadas.size(), dia);
        return marcadas;
    }

    /** Agendados do dia que o funcionário pode consultar (apenas leitura). */
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> agendadosDoDia(LocalDate dia) {
        return reservaRepository
                .findByDataEntradaLessThanEqualAndDataSaidaGreaterThanEqualAndStatusInOrderByDataEntradaAscHoraEntradaAsc(
                        dia, dia, ReservaService.STATUS_ATIVOS)
                .stream()
                .map(reservaMapper::toResponseDTO)
                .toList();
    }

    /** Contagem de agendados por tipo no dia (para a tela do funcionário). */
    @Transactional(readOnly = true)
    public long contarAgendados(LocalDate dia, TipoReserva tipo) {
        return reservaRepository
                .countByTipoAndStatusInAndDataEntradaLessThanEqualAndDataSaidaGreaterThanEqual(
                        tipo, ReservaService.STATUS_ATIVOS, dia, dia);
    }
}
