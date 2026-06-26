package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.dto.tutorDTO.TutorRequestDTO;
import com.particaolar.mundo.system.dto.tutorDTO.TutorResponseDTO;
import com.particaolar.mundo.system.exception.TutorCpfAlreadyExistsException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.exception.TutorPhoneNumberAlreadyExistsException;
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

    @Transactional
    public TutorResponseDTO salvarTutor(TutorRequestDTO requestDTO) {
        if (tutorRepository.existsByCpf(requestDTO.cpf())) {
            throw new TutorCpfAlreadyExistsException("Já existe um tutor com este CPF.");
        }
        if (tutorRepository.existsByTelefone(requestDTO.telefone())) {
            throw new TutorPhoneNumberAlreadyExistsException("Já existe um tutor cadastrado com este Telefone.");
        }
        log.info("Tutor validado com sucesso: {}", requestDTO.nome());
        Tutor tutor = tutorMapper.toEntity(requestDTO);
        Tutor tutorSalvo = tutorRepository.save(tutor);
        return tutorMapper.toResponseDTO(tutorSalvo);
    }

    @Transactional(readOnly = true)
    public List<TutorResponseDTO> listarTodos() {
        return tutorRepository.findAll()
                .stream()
                .map(tutorMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarPorId (Long tutorId) {
         Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(() -> new TutorNotFoundException(tutorId));
         TutorResponseDTO responseDTO = tutorMapper.toResponseDTO(tutor);
        return responseDTO;
    }


    @Transactional
    public TutorResponseDTO atualizar(TutorRequestDTO requestDTO, Long tutorId){
        Tutor tutor = tutorRepository.findById(tutorId)
                .orElseThrow(()-> new TutorNotFoundException(tutorId));
        tutorMapper.updateEntityFromDTO(requestDTO,tutor);
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

