package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Reserva;
import com.particaolar.mundo.system.enums.StatusReserva;
import com.particaolar.mundo.system.enums.TipoReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByPetId(Long petId);

    /**
     * Reservas ativas que cobrem o período informado (sobreposição de datas).
     * Exclui a própria reserva quando reservaId != null (reagendamento).
     */
    @Query("""
            SELECT r FROM Reserva r
            WHERE r.status IN :statuses
              AND r.dataEntrada <= :dataSaida
              AND r.dataSaida >= :dataEntrada
              AND (:reservaId IS NULL OR r.id <> :reservaId)
            """)
    List<Reserva> buscarSobreposicao(@Param("statuses") List<StatusReserva> statuses,
                                     @Param("dataEntrada") LocalDate dataEntrada,
                                     @Param("dataSaida") LocalDate dataSaida,
                                     @Param("reservaId") Long reservaId);

    /** Reservas ativas de um pet em período (para impedir duplicidade do mesmo pet). */
    @Query("""
            SELECT r FROM Reserva r
            WHERE r.pet.id = :petId
              AND r.status IN :statuses
              AND r.dataEntrada <= :dataSaida
              AND r.dataSaida >= :dataEntrada
              AND (:reservaId IS NULL OR r.id <> :reservaId)
            """)
    List<Reserva> buscarPorPetNoPeriodo(@Param("petId") Long petId,
                                        @Param("statuses") List<StatusReserva> statuses,
                                        @Param("dataEntrada") LocalDate dataEntrada,
                                        @Param("dataSaida") LocalDate dataSaida,
                                        @Param("reservaId") Long reservaId);

    /** Reservas de um tipo em um dia específico (contagem para vagas). */
    long countByTipoAndStatusInAndDataEntradaLessThanEqualAndDataSaidaGreaterThanEqual(
            TipoReserva tipo, List<StatusReserva> statuses, LocalDate dataLimite, LocalDate dataInicio);

    List<Reserva> findByDataEntradaLessThanEqualAndDataSaidaGreaterThanEqualAndStatusInOrderByDataEntradaAscHoraEntradaAsc(
            LocalDate dataEntrada, LocalDate dataSaida, List<StatusReserva> statuses);

    List<Reserva> findByDataEntradaAndTipoAndStatusIn(
            LocalDate data, TipoReserva tipo, List<StatusReserva> statuses);
}
