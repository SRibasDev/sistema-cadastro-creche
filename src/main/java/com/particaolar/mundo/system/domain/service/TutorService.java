package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.dto.TutorRequestDTO;
import com.particaolar.mundo.system.dto.TutorResponseDTO;
import com.particaolar.mundo.system.exception.BusinessException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.mapper.TutorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TutorService {

    private final TutorRepository tutorRepository;
    private final TutorMapper tutorMapper;

    @Transactional(readOnly = true)
    public List<TutorResponseDTO> listarTodos() {
        return tutorRepository.findAll()
                .stream()
                .map(tutorMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public TutorResponseDTO salvarTutor(TutorRequestDTO dto) {
        if (tutorRepository.existsByCpf(dto.cpf())) {
            throw new BusinessException("Já existe um tutor com este CPF.");
        }
        if (tutorRepository.existsByTelefone(dto.telefone())) {
            throw new BusinessException("Já existe um tutor cadastrado com este Telefone.");
        }
        log.info("Tutor validado com sucesso: {}", dto.nome());
        Tutor tutor = tutorMapper.toEntity(dto);
        Tutor tutorSalvo = tutorRepository.save(tutor);
        return tutorMapper.toResponseDTO(tutorSalvo);
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarPorId (Long tutorId) {
         Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new TutorNotFoundException(tutorId));
         TutorResponseDTO dto = tutorMapper.toResponseDTO(tutor);
        return dto;
    }

    @Transactional
    public TutorResponseDTO atualizar(TutorRequestDTO requestDTO, Long tutorId){
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(()-> new TutorNotFoundException(tutorId));
        tutorMapper.updateEntitiyFromDTO(requestDTO,tutor);
        Tutor tutorAtualizado = tutorRepository.save(tutor);
        return tutorMapper.toResponseDTO(tutorAtualizado);
    }
    @Transactional
    public void deletar(Long tutorId){
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(()-> new TutorNotFoundException(tutorId));
        tutorRepository.delete(tutor);
    }
}

