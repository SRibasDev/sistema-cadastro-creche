package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Tutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {
    boolean existsByCpf(String cpf);
    boolean existsByTelefone(String telefone);
    Page<Tutor> findByAtivoTrue(Pageable pageable);
    Optional<Tutor> findByIdAndAtivoTrue(Long id);
}
