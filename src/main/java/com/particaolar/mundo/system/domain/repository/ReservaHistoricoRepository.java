package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.ReservaHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservaHistoricoRepository extends JpaRepository<ReservaHistorico, Long> {

    List<ReservaHistorico> findByReservaIdOrderByCriadoEmDesc(Long reservaId);
}
