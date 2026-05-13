package com.mundo.particaolar.system.domain.repository;

import com.mundo.particaolar.system.domain.entity.Pet;
import com.mundo.particaolar.system.domain.repository.PetRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
}