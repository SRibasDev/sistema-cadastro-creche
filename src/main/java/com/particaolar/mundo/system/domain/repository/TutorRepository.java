package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Tutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByTelefone(String telefone);
}
