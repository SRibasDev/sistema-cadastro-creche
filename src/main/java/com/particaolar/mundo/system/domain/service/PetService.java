package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.dto.request.PetRequestDTO;
import com.particaolar.mundo.system.dto.response.PetResponseDTO;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.mapper.PetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PetService {
    private final TutorRepository tutorRepository;
    private final PetRepository petRepository;
    private final PetMapper petMapper;

    @Transactional
    public PetResponseDTO salvar(PetRequestDTO requestDTO, Long tutorId) {
        Tutor tutor = tutorRepository.findByIdAndAtivoTrue(tutorId)
                .orElseThrow(() -> new TutorNotFoundException(tutorId));
        Pet pet = petMapper.toEntity(requestDTO);
        pet.setTutor(tutor);
        Pet petSalvo = petRepository.save(pet);
        log.info("Pet salvo com sucesso: {} | Tutor: {}", petSalvo.getNome(), tutor.getNome());
        return petMapper.toResponseDTO(petSalvo);
    }

    @Transactional(readOnly = true)
    public Page<PetResponseDTO> listarTodos(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        Page<Pet> petsPage = petRepository.findByAtivoTrue(pageable);
        return petsPage.map(pet -> petMapper.toResponseDTO(pet));
    }

    @Transactional(readOnly = true)
    public PetResponseDTO buscarPetPorPetId(Long petId) {
        Pet pet = petRepository.findByIdAndAtivoTrue(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        return petMapper.toResponseDTO(pet);
    }

    @Transactional(readOnly = true)
    public List<PetResponseDTO> buscarPetPorTutorId(Long tutorId) {
        if (!tutorRepository.existsById(tutorId)) {
            throw new TutorNotFoundException(tutorId);
        }
        return petRepository.findByTutorIdAndAtivoTrue(tutorId)
                .stream()
                .map(petMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public PetResponseDTO atualizar(PetRequestDTO requestDTO, Long petId) {
        Pet pet = petRepository.findByIdAndAtivoTrue(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        petMapper.updateEntityFromDTO(requestDTO, pet);
        Pet petAtualizado = petRepository.save(pet);
        return petMapper.toResponseDTO(petAtualizado);
    }

    @Transactional
    public void deletar(Long id) {
        Pet pet = petRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new PetNotFoundException(id));
        pet.setAtivo(false);
        petRepository.save(pet);
        log.info("Pet desativado: id={}", id);
    }
}
