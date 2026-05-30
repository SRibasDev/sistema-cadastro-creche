package com.particaolar.mundo.system.domain.repository;

import com.particaolar.mundo.system.domain.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
}