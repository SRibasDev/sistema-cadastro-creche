package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Vacinacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VacinacaoRepository extends JpaRepository<Vacinacao, Long> {

    List<Vacinacao> findByPetIdAndAtivoTrueOrderByDataVencimentoAsc(Long petId);

    Page<Vacinacao> findByPetIdAndAtivoTrue(Long petId, Pageable pageable);

    Optional<Vacinacao> findByIdAndAtivoTrue(Long id);

    /** Vacinas já vencidas (ativas) de todos os pets. */
    @Query("SELECT v FROM Vacinacao v WHERE v.ativo = true AND v.dataVencimento < :hoje ORDER BY v.dataVencimento ASC")
    List<Vacinacao> buscarVencidas(@Param("hoje") LocalDate hoje);

    /** Vacinas que vencem até a data limite (ativas), excluindo as já vencidas. */
    @Query("SELECT v FROM Vacinacao v WHERE v.ativo = true AND v.dataVencimento >= :hoje AND v.dataVencimento <= :limite ORDER BY v.dataVencimento ASC")
    List<Vacinacao> buscarAVencer(@Param("hoje") LocalDate hoje, @Param("limite") LocalDate limite);

    /** Vacinas vencidas (ativas) de um pet específico. */
    @Query("SELECT v FROM Vacinacao v WHERE v.ativo = true AND v.pet.id = :petId AND v.dataVencimento < :hoje ORDER BY v.dataVencimento ASC")
    List<Vacinacao> buscarVencidasDoPet(@Param("petId") Long petId, @Param("hoje") LocalDate hoje);
}
