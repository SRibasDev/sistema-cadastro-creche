package com.mundo.particaolar.system.domain.repository;

import com.mundo.particaolar.system.domain.entity.Tutor;
import com.mundo.particaolar.system.domain.repository.TutorRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TutorRepository extends JpaRepository<Tutor, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByTelefone(String telefone);
}
