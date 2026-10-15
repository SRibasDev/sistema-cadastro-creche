package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Cobranca;
import com.particaolar.mundo.system.enums.StatusCobranca;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CobrancaRepository extends JpaRepository<Cobranca, Long> {

    Page<Cobranca> findByStatus(StatusCobranca status, Pageable pageable);

    List<Cobranca> findByTutorId(Long tutorId);

    List<Cobranca> findByHospedagemId(Long hospedagemId);

    long countByStatus(StatusCobranca status);

    long countByStatusIn(List<StatusCobranca> statuses);

    @Query("SELECT COALESCE(SUM(c.valorCobrado - c.valorPago), 0) FROM Cobranca c WHERE c.status IN (:statuses)")
    BigDecimal somarSaldoDevedorPorStatus(@Param("statuses") List<StatusCobranca> statuses);

    @Query("SELECT c FROM Cobranca c WHERE c.dataEmissao >= :inicio AND c.dataEmissao <= :fim")
    List<Cobranca> buscarPorPeriodoEmissao(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
