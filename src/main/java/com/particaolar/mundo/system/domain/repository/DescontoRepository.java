package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Desconto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DescontoRepository extends JpaRepository<Desconto, Long> {

    Optional<Desconto> findByCodigoIgnoreCase(String codigo);

    boolean existsByCodigoIgnoreCase(String codigo);
}
