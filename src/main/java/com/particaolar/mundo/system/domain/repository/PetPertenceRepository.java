package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.PetPertence;
import com.particaolar.mundo.system.enums.TipoPertence;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PetPertenceRepository extends JpaRepository<PetPertence, Long> {

    List<PetPertence> findByPetIdAndAtivoTrue(Long petId);

    Page<PetPertence> findByPetIdAndAtivoTrue(Long petId, Pageable pageable);

    List<PetPertence> findByPetIdAndTipoAndAtivoTrue(Long petId, TipoPertence tipo);

    Optional<PetPertence> findByIdAndAtivoTrue(Long id);
}
