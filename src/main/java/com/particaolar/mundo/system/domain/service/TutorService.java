package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.repository.TutorRepository;
import com.particaolar.mundo.system.dto.request.TutorRequestDTO;
import com.particaolar.mundo.system.dto.response.TutorResponseDTO;
import com.particaolar.mundo.system.exception.TutorCpfAlreadyExistsException;
import com.particaolar.mundo.system.exception.TutorNotFoundException;
import com.particaolar.mundo.system.exception.TutorPhoneNumberAlreadyExistsException;
import com.particaolar.mundo.system.mapper.TutorMapper;
import com.particaolar.mundo.system.support.NormalizadorDados;
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
        // Normaliza ANTES de qualquer verificação de unicidade: "529.982.247-25" e
        // "52998224725" devem ser reconhecidos como o mesmo CPF.
        String cpf = NormalizadorDados.normalizarCpf(requestDTO.cpf());
        String telefone = NormalizadorDados.normalizarTelefone(requestDTO.telefone());

        if (tutorRepository.existsByCpf(cpf)) {
            throw new TutorCpfAlreadyExistsException();
        }
        if (tutorRepository.existsByTelefone(telefone)) {
            throw new TutorPhoneNumberAlreadyExistsException();
        }

        Tutor tutor = tutorMapper.toEntity(requestDTO);
        Tutor tutorSalvo = tutorRepository.save(tutor);
        log.info("Tutor cadastrado id={}", tutorSalvo.getId());
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

        // Normaliza para comparar com o valor já persistido (também normalizado).
        String cpf = NormalizadorDados.normalizarCpf(requestDTO.cpf());
        String telefone = NormalizadorDados.normalizarTelefone(requestDTO.telefone());

        if (!cpf.equals(tutor.getCpf()) && tutorRepository.existsByCpf(cpf)) {
            throw new TutorCpfAlreadyExistsException();
        }

        if (!telefone.equals(tutor.getTelefone()) && tutorRepository.existsByTelefone(telefone)) {
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

