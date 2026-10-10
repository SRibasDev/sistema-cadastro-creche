package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.CapacidadeDia;
import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.domain.repository.CapacidadeDiaRepository;
import com.particaolar.mundo.system.domain.repository.ReservaRepository;
import com.particaolar.mundo.system.enums.StatusReserva;
import com.particaolar.mundo.system.enums.TipoReserva;
import com.particaolar.mundo.system.exception.CapacidadeExcedidaException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Regras de capacidade (vagas) por dia e tipo.
 * Concorrência: o lock pessimista na linha do dia serializa criações
 * simultâneas; se a linha não existe, tentamos criá-la (unique key) e
 * reexecutamos o lock — evita oversell mesmo com requisições paralelas.
 * SaaS: a capacidade futura será por (tenant_id, data).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CapacidadeService {

    private static final List<StatusReserva> STATUS_QUE_OCUPAM_VAGA =
            List.of(StatusReserva.AGENDADA, StatusReserva.EM_ANDAMENTO);

    private final CapacidadeDiaRepository capacidadeDiaRepository;
    private final ReservaRepository reservaRepository;

    @Value("${capacidade.vagas-padrao:50}")
    private int vagasPadrao;

    /** Capacidade vigente para um dia (override ou padrão). */
    @Transactional
    public int capacidadeDoDia(LocalDate dia) {
        return capacidadeDiaRepository.findByData(dia)
                .map(CapacidadeDia::getVagas)
                .orElse(vagasPadrao);
    }

    /** Ocupação atual (reservas ativas cobrindo o dia). */
    @Transactional(readOnly = true)
    public long ocupacaoDoDia(LocalDate dia, TipoReserva tipo) {
        return reservaRepository.countByTipoAndStatusInAndDataEntradaLessThanEqualAndDataSaidaGreaterThanEqual(
                tipo, STATUS_QUE_OCUPAM_VAGA, dia, dia);
    }

    /**
     * Valida se é possível criar/atualizar uma reserva cobrindo cada dia do
     * período informado. Lança CapacidadeExcedidaException quando lotado.
     * reservaId permite ignorar a própria reserva no reagendamento.
     */
    @Transactional
    public void validarVagas(TipoReserva tipo, LocalDate dataEntrada, LocalDate dataSaida, Long reservaId) {
        for (LocalDate dia = dataEntrada; !dia.isAfter(dataSaida); dia = dia.plusDays(1)) {
            garantirLinhaDoDia(dia); // garante lock serializável do dia
            long ocupadas = reservaRepository.countByTipoAndStatusInAndDataEntradaLessThanEqualAndDataSaidaGreaterThanEqual(
                    tipo, STATUS_QUE_OCUPAM_VAGA, dia, dia);
            int capacidade = capacidadeDoDia(dia);
            // Na prática a reserva ainda não foi salva, então a contagem não a inclui.
            if (ocupadas >= capacidade) {
                throw new CapacidadeExcedidaException(dia, capacidade);
            }
        }
    }

    /** Contagem de reservas de um tipo em um dia (para a visão do dia). */
    @Transactional(readOnly = true)
    public long contarPorDia(LocalDate dia, TipoReserva tipo) {
        return reservaRepository.countByTipoAndStatusInAndDataEntradaLessThanEqualAndDataSaidaGreaterThanEqual(
                tipo, STATUS_QUE_OCUPAM_VAGA, dia, dia);
    }

    private void garantirLinhaDoDia(LocalDate dia) {
        if (capacidadeDiaRepository.findByData(dia).isPresent()) {
            return; // linha já existe e está lockada
        }
        try {
            CapacidadeDia nova = new CapacidadeDia(dia, vagasPadrao, null);
            capacidadeDiaRepository.saveAndFlush(nova);
        } catch (DataIntegrityViolationException e) {
            // Outra transação criou a linha concurrently — apenas locka novamente.
            capacidadeDiaRepository.findByData(dia);
        }
    }
}
