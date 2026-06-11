package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.dto.PetRequestDTO;
import com.particaolar.mundo.system.dto.PetResponseDTO;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
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

    @Transactional
    public PetResponseDTO salvar(PetRequestDTO petRequestDTO, Long tutorId){
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new TutorNotFoundException(tutorId));
        Pet pet = petMapper.toEntity(petRequestDTO);
        pet.setTutor(tutor);
        Pet petSalvo = petRepository.save(pet);
        return petMapper.toResponseDTO(petSalvo);
    }

    @Transactional (readOnly = true)
    public List<PetResponseDTO> listarTodos (){
        List<Pet> pets = petRepository.findAll();
        return pets.stream()
                .map(petMapper::toResponseDTO)
                .toList();
    }

    @Transactional (readOnly = true)
    public PetResponseDTO buscarPetPorPetId(Long petId){
        Pet pet = petRepository.findById(petId).orElseThrow(() -> new PetNotFoundException(petId));
        return petMapper.toResponseDTO(pet);
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> buscarPetPorTutorId(Long tutorId) {
        return petRepository.findByTutorId(tutorId)
                .stream()
                .map(petMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public PetResponseDTO atualizar(PetRequestDTO requestDTO, Long petId){
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        petMapper.updateEntityFromDTO(requestDTO,pet);
        Pet petAtualizado = petRepository.save(pet);
        return petMapper.toResponseDTO(petAtualizado);
    }

    @Transactional
    public void deletar(Long id){
        Pet pet = petRepository.findById(id)
                .orElseThrow(() -> new PetNotFoundException(id));
        petRepository.delete(pet);
    }
}
