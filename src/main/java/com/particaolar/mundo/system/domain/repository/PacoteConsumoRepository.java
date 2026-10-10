package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.PacoteConsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PacoteConsumoRepository extends JpaRepository<PacoteConsumo, Long> {

    List<PacoteConsumo> findByPacoteCompradoId(Long pacoteCompradoId);

    boolean existsByReservaId(Long reservaId);
}
