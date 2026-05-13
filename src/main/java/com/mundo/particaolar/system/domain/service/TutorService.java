package com.mundo.particaolar.system.domain.service;

import com.mundo.particaolar.system.domain.entity.Tutor;
import com.mundo.particaolar.system.domain.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository tutorRepository;

    public Tutor salvarTutor(Tutor tutor) {

        if (tutorRepository.existsByCpf(tutor.getCpf())) {
            throw new IllegalArgumentException("Já existe um tutor cadastrado com este CPF.");
        }

        if (tutorRepository.existsByTelefone(tutor.getTelefone())) {
            throw new IllegalArgumentException("Já existe um tutor cadastrado com este Telefone.");
        }

        System.out.println("Tutor validado com sucesso: " + tutor.getNome());
        return tutorRepository.save(tutor);
    }

    public List<Tutor> listarTodos() {
        return tutorRepository.findAll();
    }

    public Tutor buscarPorId(Long id) {
        return tutorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tutor não encontrado!"));
    }
}