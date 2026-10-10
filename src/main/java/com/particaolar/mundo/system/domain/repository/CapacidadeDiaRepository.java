package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.CapacidadeDia;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface CapacidadeDiaRepository extends JpaRepository<CapacidadeDia, LocalDate> {

    /**
     * Lock pessimista usado na validação de vagas para serializar criações
     * simultâneas do mesmo dia (evita oversell com requisições concorrentes).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CapacidadeDia> findByData(LocalDate data);
}
