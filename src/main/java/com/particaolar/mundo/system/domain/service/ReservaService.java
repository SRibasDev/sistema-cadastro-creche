package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.domain.entity.ReservaHistorico;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.ReservaHistoricoRepository;
import com.particaolar.mundo.system.domain.repository.ReservaRepository;
import com.particaolar.mundo.system.dto.request.ReagendarRequestDTO;
import com.particaolar.mundo.system.dto.request.ReservaRequestDTO;
import com.particaolar.mundo.system.dto.response.ReservaResponseDTO;
import com.particaolar.mundo.system.dto.response.ReservaHistoricoResponseDTO;
import com.particaolar.mundo.system.enums.StatusReserva;
import com.particaolar.mundo.system.enums.TipoReserva;
import com.particaolar.mundo.system.exception.PetComReservaNoPeriodoException;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.ReservaNotFoundException;
import com.particaolar.mundo.system.mapper.ReservaMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regras de Reserva (creche/hotel): criação com validação de vagas e
 * sobreposição, reagendamento com histórico, cancelamento com motivo e
 * máquina de status.
 * SaaS: todas as consultas passam por aqui; quando existir tenant, o filtro
 * entrará nos repositories e será propagado a partir do JWT.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReservaService {

    static final List<StatusReserva> STATUS_ATIVOS =
            List.of(StatusReserva.AGENDADA, StatusReserva.EM_ANDAMENTO);

    /** Transições válidas: de -> para. */
    private static final Map<StatusReserva, Set<StatusReserva>> TRANSICOES = Map.of(
            StatusReserva.AGENDADA, Set.of(StatusReserva.EM_ANDAMENTO, StatusReserva.FALTOU,
                    StatusReserva.CANCELADA),
            StatusReserva.EM_ANDAMENTO, Set.of(StatusReserva.CONCLUIDA, StatusReserva.CANCELADA),
            StatusReserva.FALTOU, Set.of(),
            StatusReserva.CONCLUIDA, Set.of(),
            StatusReserva.CANCELADA, Set.of()
    );

    private final ReservaRepository reservaRepository;
    private final ReservaHistoricoRepository historicoRepository;
    private final PetRepository petRepository;
    private final ReservaMapper reservaMapper;
    private final CapacidadeService capacidadeService;

    @Transactional
    public ReservaResponseDTO salvar(ReservaRequestDTO dto) {
        validarPeriodo(dto.dataEntrada(), dto.dataSaida(), dto.horaEntrada(), dto.horaSaida());

        Pet pet = petRepository.findByIdAndAtivoTrue(dto.petId())
                .orElseThrow(() -> new PetNotFoundException(dto.petId()));

        if (!reservaRepository.buscarPorPetNoPeriodo(pet.getId(), STATUS_ATIVOS,
                dto.dataEntrada(), dto.dataSaida(), null).isEmpty()) {
            throw new PetComReservaNoPeriodoException(pet.getId());
        }

        capacidadeService.validarVagas(dto.tipo(), dto.dataEntrada(), dto.dataSaida(), null);

        Reserva reserva = reservaMapper.toEntity(dto);
        reserva.setPet(pet);
        reserva.setStatus(StatusReserva.AGENDADA);
        Reserva salva = reservaRepository.save(reserva);

        registrarHistorico(salva, "CRIACAO", null, null, null, null, null, null, null);

        log.info("Reserva {} criada: pet={} tipo={} entrada={} saida={}",
                salva.getId(), pet.getNome(), dto.tipo(), dto.dataEntrada(), dto.dataSaida());
        return reservaMapper.toResponseDTO(salva);
    }

    @Transactional(readOnly = true)
    public Page<ReservaResponseDTO> listarTodas(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return reservaRepository.findAll(pageable).map(reservaMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ReservaResponseDTO buscarPorId(Long id) {
        return reservaMapper.toResponseDTO(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> buscarPorPetId(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new PetNotFoundException(petId);
        }
        return reservaRepository.findByPetId(petId).stream()
                .map(reservaMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public ReservaResponseDTO atualizar(Long id, ReservaRequestDTO dto) {
        Reserva reserva = buscarEntidade(id);
        if (reserva.getStatus() != StatusReserva.AGENDADA) {
            throw new IllegalStateException(
                    "Só é possível editar reservas AGENDADA (atual: " + reserva.getStatus() + ")");
        }
        validarPeriodo(dto.dataEntrada(), dto.dataSaida(), dto.horaEntrada(), dto.horaSaida());

        if (!reservaRepository.buscarPorPetNoPeriodo(dto.petId(), STATUS_ATIVOS,
                dto.dataEntrada(), dto.dataSaida(), id).isEmpty()) {
            throw new PetComReservaNoPeriodoException(dto.petId());
        }

        capacidadeService.validarVagas(dto.tipo(), dto.dataEntrada(), dto.dataSaida(), id);

        LocalDate dataAnterior = reserva.getDataEntrada();
        LocalDate dataSaidaAnterior = reserva.getDataSaida();
        reservaMapper.updateEntityFromDTO(dto, reserva);
        log.info("Reserva {} atualizada", id);
        if (!dataAnterior.equals(reserva.getDataEntrada())
                || !dataSaidaAnterior.equals(reserva.getDataSaida())) {
            registrarHistorico(reserva, "REAGENDAMENTO", dataAnterior, dataSaidaAnterior,
                    reserva.getDataEntrada(), reserva.getDataSaida(), null, null, null);
        }
        return reservaMapper.toResponseDTO(reserva);
    }

    /** Reagendamento explícito: muda datas/horários e registra histórico com motivo. */
    @Transactional
    public ReservaResponseDTO reagendar(Long id, ReagendarRequestDTO dto) {
        Reserva reserva = buscarEntidade(id);
        if (reserva.getStatus() != StatusReserva.AGENDADA) {
            throw new IllegalStateException(
                    "Só é possível reagendar reservas AGENDADA (atual: " + reserva.getStatus() + ")");
        }

        LocalDate novaEntrada = dto.dataEntrada() != null ? dto.dataEntrada() : reserva.getDataEntrada();
        LocalDate novaSaida = dto.dataSaida() != null ? dto.dataSaida() : reserva.getDataSaida();
        validarPeriodo(novaEntrada, novaSaida, reserva.getHoraEntrada(), reserva.getHoraSaida());

        if (!reservaRepository.buscarPorPetNoPeriodo(reserva.getPet().getId(), STATUS_ATIVOS,
                novaEntrada, novaSaida, id).isEmpty()) {
            throw new PetComReservaNoPeriodoException(reserva.getPet().getId());
        }
        capacidadeService.validarVagas(reserva.getTipo(), novaEntrada, novaSaida, id);

        LocalDate dataAnterior = reserva.getDataEntrada();
        LocalDate dataSaidaAnterior = reserva.getDataSaida();
        reserva.setDataEntrada(novaEntrada);
        reserva.setDataSaida(novaSaida);
        if (dto.horaEntrada() != null) reserva.setHoraEntrada(dto.horaEntrada());
        if (dto.horaSaida() != null) reserva.setHoraSaida(dto.horaSaida());

        registrarHistorico(reserva, "REAGENDAMENTO", dataAnterior, dataSaidaAnterior,
                novaEntrada, novaSaida, null, null, dto.motivo());
        log.info("Reserva {} reagendada: {} -> {}", id, dataAnterior, novaEntrada);
        return reservaMapper.toResponseDTO(reserva);
    }

    /** Cancelamento lógico com motivo obrigatório. */
    @Transactional
    public void cancelar(Long id, String motivo) {
        Reserva reserva = buscarEntidade(id);
        if (reserva.getStatus() == StatusReserva.CANCELADA) {
            throw new IllegalStateException("Reserva já está cancelada");
        }
        if (reserva.getStatus() == StatusReserva.CONCLUIDA) {
            throw new IllegalStateException("Não é possível cancelar uma reserva concluída");
        }
        StatusReserva anterior = reserva.getStatus();
        reserva.setStatus(StatusReserva.CANCELADA);
        reserva.setMotivoCancelamento(motivo);
        registrarHistorico(reserva, "CANCELAMENTO", reserva.getDataEntrada(), reserva.getDataSaida(),
                null, null, anterior.name(), StatusReserva.CANCELADA.name(), motivo);
        log.info("Reserva {} cancelada. Motivo: {}", id, motivo);
    }

    @Transactional
    public ReservaResponseDTO atualizarStatus(Long id, StatusReserva novoStatus) {
        Reserva reserva = buscarEntidade(id);
        if (reserva.getStatus() == novoStatus) {
            return reservaMapper.toResponseDTO(reserva);
        }
        if (!TRANSICOES.get(reserva.getStatus()).contains(novoStatus)) {
            throw new IllegalStateException(
                    "Transição de status inválida: " + reserva.getStatus() + " -> " + novoStatus);
        }
        StatusReserva anterior = reserva.getStatus();
        reserva.setStatus(novoStatus);
        registrarHistorico(reserva, "STATUS_ALTERADO", null, null, null, null,
                anterior.name(), novoStatus.name(), null);
        log.info("Status da reserva {} atualizado: {} -> {}", id, anterior, novoStatus);
        return reservaMapper.toResponseDTO(reserva);
    }

    /** Visão do dia: reservas ativas cobrindo a data. */
    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> buscarPorDia(LocalDate dia) {
        return reservaRepository
                .findByDataEntradaLessThanEqualAndDataSaidaGreaterThanEqualAndStatusInOrderByDataEntradaAscHoraEntradaAsc(
                        dia, dia, STATUS_ATIVOS)
                .stream()
                .map(reservaMapper::toResponseDTO)
                .toList();
    }

    /** Lotação do dia por tipo (ocupadas / capacidade / livres). */
    @Transactional(readOnly = true)
    public Map<String, Object> resumoDoDia(LocalDate dia) {
        Map<String, Object> resumo = new LinkedHashMap<>();
        resumo.put("data", dia);
        for (TipoReserva tipo : TipoReserva.values()) {
            long ocupadas = capacidadeService.contarPorDia(dia, tipo);
            int capacidade = capacidadeService.capacidadeDoDia(dia);
            resumo.put(tipo.name().toLowerCase(),
                    Map.of("ocupadas", ocupadas, "capacidade", capacidade,
                            "livres", Math.max(0, capacidade - ocupadas)));
        }
        return resumo;
    }

    Reserva buscarEntidade(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ReservaNotFoundException(id));
    }

    /** Histórico de eventos de uma reserva (reagendamentos, cancelamentos...). */
    @Transactional(readOnly = true)
    public List<ReservaHistoricoResponseDTO> buscarHistoricos(Long id) {
        buscarEntidade(id); // 404 se não existir
        return historicoRepository.findByReservaIdOrderByCriadoEmDesc(id).stream()
                .map(h -> new ReservaHistoricoResponseDTO(
                        h.getId(), h.getReserva().getId(), h.getTipoEvento(),
                        h.getDataAnterior(), h.getDataSaidaAnterior(),
                        h.getDataNova(), h.getDataSaidaNova(),
                        h.getStatusAnterior(), h.getStatusNovo(),
                        h.getMotivo(), h.getUsuario(), h.getCriadoEm()))
                .toList();
    }

    private void validarPeriodo(LocalDate dataEntrada, LocalDate dataSaida,
                                java.time.LocalTime horaEntrada, java.time.LocalTime horaSaida) {
        if (dataEntrada == null || dataSaida == null) {
            throw new IllegalArgumentException("Datas de entrada e saída são obrigatórias");
        }
        if (dataSaida.isBefore(dataEntrada)) {
            throw new IllegalArgumentException("A data de saída não pode ser anterior à data de entrada");
        }
        if (horaEntrada != null && horaSaida != null && !horaSaida.isAfter(horaEntrada)) {
            throw new IllegalArgumentException("O horário de saída deve ser posterior ao de entrada");
        }
    }

    private void registrarHistorico(Reserva reserva, String tipoEvento,
                                    LocalDate dataAnterior, LocalDate dataSaidaAnterior,
                                    LocalDate dataNova, LocalDate dataSaidaNova,
                                    String statusAnterior, String statusNovo, String motivo) {
        ReservaHistorico h = new ReservaHistorico();
        h.setReserva(reserva);
        h.setTipoEvento(tipoEvento);
        h.setDataAnterior(dataAnterior);
        h.setDataSaidaAnterior(dataSaidaAnterior);
        h.setDataNova(dataNova);
        h.setDataSaidaNova(dataSaidaNova);
        h.setStatusAnterior(statusAnterior);
        h.setStatusNovo(statusNovo);
        h.setMotivo(motivo);
        historicoRepository.save(h);
    }
}