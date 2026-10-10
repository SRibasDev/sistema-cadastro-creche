package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.PetPertence;
import com.particaolar.mundo.system.domain.repository.PetPertenceRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.dto.request.PetPertenceRequestDTO;
import com.particaolar.mundo.system.dto.response.PetPertenceResponseDTO;
import com.particaolar.mundo.system.enums.TipoPertence;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.PetPertenceNotFoundException;
import com.particaolar.mundo.system.mapper.PetPertenceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * CRUD de pertences do cão e atualização de alertas/observações do pet.
 * Escrita: ADMIN (regras em SecurityConfig). Leitura: ambos os perfis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PetPertenceService {

    private final PetPertenceRepository pertenceRepository;
    private final PetRepository petRepository;
    private final PetPertenceMapper pertenceMapper;

    @Transactional
    public PetPertenceResponseDTO salvar(Long petId, PetPertenceRequestDTO dto) {
        Pet pet = petRepository.findByIdAndAtivoTrue(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        PetPertence pertence = pertenceMapper.toEntity(dto);
        pertence.setPet(pet);
        PetPertence salvo = pertenceRepository.save(pertence);
        log.info("Pertence '{}' cadastrado para pet {}", salvo.getTipo(), petId);
        return pertenceMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public List<PetPertenceResponseDTO> listarPorPet(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new PetNotFoundException(petId);
        }
        return pertenceRepository.findByPetIdAndAtivoTrue(petId).stream()
                .map(pertenceMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PetPertenceResponseDTO> listarPorPetETipo(Long petId, TipoPertence tipo) {
        if (!petRepository.existsById(petId)) {
            throw new PetNotFoundException(petId);
        }
        return pertenceRepository.findByPetIdAndTipoAndAtivoTrue(petId, tipo).stream()
                .map(pertenceMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<PetPertenceResponseDTO> listarPaginado(Long petId, Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return pertenceRepository.findByPetIdAndAtivoTrue(petId, pageable)
                .map(pertenceMapper::toResponseDTO);
    }

    @Transactional
    public PetPertenceResponseDTO atualizar(Long id, PetPertenceRequestDTO dto) {
        PetPertence pertence = pertenceRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new PetPertenceNotFoundException(id));
        pertenceMapper.updateEntityFromDTO(dto, pertence);
        log.info("Pertence {} atualizado", id);
        return pertenceMapper.toResponseDTO(pertence);
    }

    @Transactional
    public void deletar(Long id) {
        PetPertence pertence = pertenceRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new PetPertenceNotFoundException(id));
        pertence.setAtivo(false);
        log.info("Pertence {} desativado", id);
    }
}
