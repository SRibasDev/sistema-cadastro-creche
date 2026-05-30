package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.dto.PetRequestDTO;
import com.particaolar.mundo.system.dto.PetResponseDTO;
import com.particaolar.mundo.system.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;


@Service
@RequiredArgsConstructor
public class PetService {
    private final TutorRepository tutorRepository;
    private final PetRepository petRepository;
    private final PetMapper petMapper;

    @Transactional (readOnly = true)
    public List<PetResponseDTO> listarTodos (){
        List<Pet> pets = petRepository.findAll();
        return pets.stream()
                .map(petMapper::toResponseDTO)
                .toList();
    }

    @Transactional (readOnly = true)
    public PetResponseDTO buscarPorId(Long id){
        Pet pet = petRepository.findById(id).orElseThrow(() -> new RuntimeException("Pet não encontrado com o ID: " +id));
        return petMapper.toResponseDTO(pet);
    }

    @Transactional
    public PetResponseDTO salvar(PetRequestDTO petRequestDTO, Long tutorId){
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado"));
        Pet pet = petMapper.toEntity(petRequestDTO);
        pet.setTutor(tutor);

        Pet petSalvo = petRepository.save(pet);

        return petMapper.toResponseDTO(petSalvo);
    }

    @Transactional
    public void deletar(Long id){
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pet não encontrado com o id: " + id));
        petRepository.delete(pet);
    }
}
