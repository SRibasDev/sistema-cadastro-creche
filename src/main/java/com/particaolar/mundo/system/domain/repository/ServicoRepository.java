package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Servico;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    Page<Servico> findByAtivoTrue(Pageable pageable);

    List<Servico> findByAtivoTrue();

    Optional<Servico> findByIdAndAtivoTrue(Long id);

    boolean existsByNome(String nome);
}
