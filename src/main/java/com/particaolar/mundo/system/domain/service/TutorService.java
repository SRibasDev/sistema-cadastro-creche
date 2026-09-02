package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.dto.request.TutorRequestDTO;
import com.particaolar.mundo.system.dto.response.TutorResponseDTO;
import com.particaolar.mundo.system.exception.TutorCpfAlreadyExistsException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.exception.TutorPhoneNumberAlreadyExistsException;
import com.particaolar.mundo.system.mapper.TutorMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Slf4j
public class TutorService {

    private final TutorRepository tutorRepository;
    private final TutorMapper tutorMapper;

    @Transactional
    public TutorResponseDTO salvarTutor(TutorRequestDTO requestDTO) {
        if (tutorRepository.existsByCpf(requestDTO.cpf())) {
            throw new TutorCpfAlreadyExistsException();
        }
        if (tutorRepository.existsByTelefone(requestDTO.telefone())) {
            throw new TutorPhoneNumberAlreadyExistsException();
        }
        log.info("Tutor validado com sucesso: {}", requestDTO.nome());
        Tutor tutor = tutorMapper.toEntity(requestDTO);
        Tutor tutorSalvo = tutorRepository.save(tutor);
        return tutorMapper.toResponseDTO(tutorSalvo);
    }

    @Transactional(readOnly = true)
    public Page<TutorResponseDTO> listarTodos(Pageable pageable) {
        // Enforce max page size
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        Page<Tutor> tutoresPage = tutorRepository.findByAtivoTrue(pageable);
        return tutoresPage.map(tutor -> tutorMapper.toResponseDTO(tutor));
    }

    @Transactional(readOnly = true)
    public TutorResponseDTO buscarPorId(Long tutorId) {
        Tutor tutor = tutorRepository.findByIdAndAtivoTrue(tutorId)
                .orElseThrow(() -> new TutorNotFoundException(tutorId));
        return tutorMapper.toResponseDTO(tutor);
    }

    @Transactional
    public TutorResponseDTO atualizar(TutorRequestDTO requestDTO, Long tutorId) {
        Tutor tutor = tutorRepository.findByIdAndAtivoTrue(tutorId)
                .orElseThrow(() -> new TutorNotFoundException(tutorId));

        if (!tutor.getCpf().equals(requestDTO.cpf()) &&
                tutorRepository.existsByCpf(requestDTO.cpf())) {
            throw new TutorCpfAlreadyExistsException();
        }

        if (!tutor.getTelefone().equals(requestDTO.telefone()) &&
                tutorRepository.existsByTelefone(requestDTO.telefone())) {
            throw new TutorPhoneNumberAlreadyExistsException();
        }

        tutorMapper.updateEntityFromDTO(requestDTO, tutor);
        log.info("Tutor {} atualizado", tutorId);
        return tutorMapper.toResponseDTO(tutor);
    }

    @Transactional
    public void deletar(Long id) {
        Tutor tutor = tutorRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new TutorNotFoundException(id));
        tutor.setAtivo(false);
        tutorRepository.save(tutor);
        log.info("Tutor desativado: id={}", id);
    }

}

