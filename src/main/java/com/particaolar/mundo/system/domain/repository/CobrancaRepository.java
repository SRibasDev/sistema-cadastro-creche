package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Cobranca;
import com.particaolar.mundo.system.enums.StatusCobranca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {

    Page<Cobranca> findByStatus(StatusCobranca status, Pageable pageable);

    Page<Cobranca> findByTutorId(Long tutorId, Pageable pageable);

    Page<Cobranca> findByPetId(Long petId, Pageable pageable);

    List<Cobranca> findByStatusAndDataVencimentoLessThan(
            StatusCobranca status, LocalDate data);

    boolean existsByReservaIdAndStatusNot(Long reservaId, StatusCobranca status);

    /** Vencendo em uma data específica com status PENDENTE (para o job de e-mail). */
    @Query("""
            SELECT c FROM Cobranca c
            WHERE c.status = :status
              AND c.dataVencimento = :data
            """)
    List<Cobranca> buscarPorStatusEVencimento(
            @Param("status") StatusCobranca status, @Param("data") LocalDate data);
}
