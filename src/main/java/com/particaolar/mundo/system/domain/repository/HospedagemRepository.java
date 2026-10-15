package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.enums.Modalidade;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface HospedagemRepository extends JpaRepository<Hospedagem, Long> {

    List<Hospedagem> findByPetId(Long petId);

    /**
     * Existe sobreposição de intervalo para o pet?
     * Intervalo semifechado [entrada, saida): conflita quando
     * existente.entrada &lt; novaSaida AND novaEntrada &lt; existente.saida.
     * Reservas adjacentes (novaEntrada == existente.saida) NÃO conflitam.
     */
    @Query("""
            SELECT COUNT(h) > 0 FROM Hospedagem h
            WHERE h.pet.id = :petId
              AND h.status IN :statuses
              AND h.dataEntrada < :saida
              AND h.dataSaida > :entrada
            """)
    boolean existeSobreposicao(@Param("petId") Long petId,
                               @Param("statuses") Collection<StatusHospedagem> statuses,
                               @Param("entrada") LocalDateTime entrada,
                               @Param("saida") LocalDateTime saida);

    /** Igual a {@link #existeSobreposicao} mas ignora a própria reserva (reagendamento). */
    @Query("""
            SELECT COUNT(h) > 0 FROM Hospedagem h
            WHERE h.pet.id = :petId
              AND h.status IN :statuses
              AND h.id <> :excluindoId
              AND h.dataEntrada < :saida
              AND h.dataSaida > :entrada
            """)
    boolean existeSobreposicaoExceto(@Param("petId") Long petId,
                                     @Param("statuses") Collection<StatusHospedagem> statuses,
                                     @Param("entrada") LocalDateTime entrada,
                                     @Param("saida") LocalDateTime saida,
                                     @Param("excluindoId") Long excluindoId);

    /**
     * Conta reservas ativas de uma modalidade que ocupam o dia de calendário
     * [dia, dia+1). Ocupação é inclusiva nas duas pontas da reserva:
     * date(entrada) &lt;= dia AND date(saida) &gt;= dia, ou seja
     * entrada &lt; dia+1 AND saida &gt;= dia.
     */
    @Query("""
            SELECT COUNT(h) FROM Hospedagem h
            WHERE h.modalidade = :modalidade
              AND h.status IN :statuses
              AND h.dataEntrada < :diaMaisUm
              AND h.dataSaida >= :dia
            """)
    long contarNoDia(@Param("modalidade") Modalidade modalidade,
                     @Param("statuses") Collection<StatusHospedagem> statuses,
                     @Param("dia") LocalDateTime dia,
                     @Param("diaMaisUm") LocalDateTime diaMaisUm);

    /** Igual a {@link #contarNoDia} mas ignora a própria reserva (reagendamento). */
    @Query("""
            SELECT COUNT(h) FROM Hospedagem h
            WHERE h.modalidade = :modalidade
              AND h.status IN :statuses
              AND h.id <> :excluindoId
              AND h.dataEntrada < :diaMaisUm
              AND h.dataSaida >= :dia
            """)
    long contarNoDiaExceto(@Param("modalidade") Modalidade modalidade,
                           @Param("statuses") Collection<StatusHospedagem> statuses,
                           @Param("dia") LocalDateTime dia,
                           @Param("diaMaisUm") LocalDateTime diaMaisUm,
                           @Param("excluindoId") Long excluindoId);

    boolean existsByPetIdAndStatusIn(Long petId, Collection<StatusHospedagem> statuses);

    long countByStatus(StatusHospedagem status);

    List<Hospedagem> findByStatus(StatusHospedagem status);

    List<Hospedagem> findTop10ByStatusAndDataEntradaAfterOrderByDataEntradaAsc(
            StatusHospedagem status, LocalDateTime agora);

    long countByStatusAndDataEntradaBetween(StatusHospedagem status, LocalDateTime inicio, LocalDateTime fim);

    long countByStatusAndDataSaidaBetween(StatusHospedagem status, LocalDateTime inicio, LocalDateTime fim);

    List<Hospedagem> findByDataEntradaBetween(LocalDateTime inicio, LocalDateTime fim);
}