package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HospedagemRepository extends JpaRepository<Hospedagem, Long> {

    List<Hospedagem> findByPetId(Long petId);

    boolean existsByPetIdAndStatusIn(Long petId, List<StatusHospedagem> statuses);
}