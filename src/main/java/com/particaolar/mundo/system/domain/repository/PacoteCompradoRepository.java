package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.PacoteComprado;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PacoteCompradoRepository extends JpaRepository<PacoteComprado, Long> {

    List<PacoteComprado> findByPetIdAndAtivoTrue(Long petId);

    /**
     * Pacotes ativos com saldo e validade em ordem de validade mais próxima.
     * Usado para escolher de onde debitar na presença.
     */
    @Query("""
            SELECT p FROM PacoteComprado p
            WHERE p.pet.id = :petId
              AND p.ativo = true
              AND p.saldo > 0
              AND p.validade >= :hoje
            ORDER BY p.validade ASC
            """)
    List<PacoteComprado> buscarComSaldo(@Param("petId") Long petId,
                                        @Param("hoje") LocalDate hoje);

    /**
     * Lock pessimista para o débito atômico de saldo (evita saldo negativo
     * com dois funcionários marcando presença ao mesmo tempo).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PacoteComprado p WHERE p.id = :id")
    Optional<PacoteComprado> findWithLockById(@Param("id") Long id);
}
