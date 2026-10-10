package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Pacote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PacoteRepository extends JpaRepository<Pacote, Long> {

    Page<Pacote> findByAtivoTrue(Pageable pageable);

    Optional<Pacote> findByIdAndAtivoTrue(Long id);

    boolean existsByNome(String nome);
}
