package com.mundo.particaolar.system.domain.service;

import com.mundo.particaolar.system.domain.entity.Pet;
import com.mundo.particaolar.system.domain.entity.Tutor;
import com.mundo.particaolar.system.domain.repository.PetRepository;
import com.mundo.particaolar.system.domain.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PetService {
    private final TutorRepository tutorRepository;
    private final PetRepository petRepository;


    public Pet salvarPet(Long tutorId, Pet pet){
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado!"));
        pet.setTutor(tutor);
        return petRepository.save(pet);
    }

}
