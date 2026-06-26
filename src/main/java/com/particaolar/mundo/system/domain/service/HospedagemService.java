package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.dto.hospedagemDTO.HospedagemRequestDTO;
import com.particaolar.mundo.system.dto.hospedagemDTO.HospedagemResponseDTO;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import com.particaolar.mundo.system.exception.HospedagemNotFoundException;
import com.particaolar.mundo.system.exception.PetJaHospedadoException;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.mapper.HospedagemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospedagemService {

    private final HospedagemRepository hospedagemRepository;
    private final PetRepository petRepository;
    private final HospedagemMapper hospedagemMapper;

    @Transactional
    public HospedagemResponseDTO salvar(HospedagemRequestDTO dto) {
        Pet pet = petRepository.findById(dto.petId())
                .orElseThrow(() -> new PetNotFoundException(dto.petId()));

        boolean jaHospedado = hospedagemRepository
                .existsByPetIdAndStatusIn(dto.petId(), List.of(
                        StatusHospedagem.AGENDADA,
                        StatusHospedagem.EM_ANDAMENTO
                ));

        if (jaHospedado) {
            throw new PetJaHospedadoException(dto.petId());
        }

        Hospedagem hospedagem = hospedagemMapper.toEntity(dto);
        hospedagem.setPet(pet);
        Hospedagem hospedagemSalva = hospedagemRepository.save(hospedagem);
        log.info("Hospedagem criada: pet={} | entrada={} | saida={}",
                pet.getNome(), dto.dataEntrada(), dto.dataSaida());
        return hospedagemMapper.toResponseDTO(hospedagemSalva);
    }

    @Transactional(readOnly = true)
    public List<HospedagemResponseDTO> listarTodas() {
        return hospedagemRepository.findAll()
                .stream()
                .map(hospedagemMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public HospedagemResponseDTO buscarPorId(Long id) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    @Transactional(readOnly = true)
    public List<HospedagemResponseDTO> buscarPorPetId(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new PetNotFoundException(petId);
        }
        return hospedagemRepository.findByPetId(petId)
                .stream()
                .map(hospedagemMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public HospedagemResponseDTO atualizarStatus(Long id, StatusHospedagem novoStatus) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));
        hospedagem.setStatus(novoStatus);
        log.info("Status da hospedagem {} atualizado para {}", id, novoStatus);
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    @Transactional
    public HospedagemResponseDTO atualizar(Long id, HospedagemRequestDTO dto) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));
        hospedagemMapper.updateEntityFromDTO(dto, hospedagem);
        log.info("Hospedagem {} atualizada", id);
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    @Transactional
    public void cancelar(Long id) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));
        hospedagem.setStatus(StatusHospedagem.CANCELADA);
        log.info("Hospedagem {} cancelada", id);
    }
}