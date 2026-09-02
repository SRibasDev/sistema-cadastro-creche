package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Hospedagem;
import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.repository.HospedagemRepository;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.dto.request.HospedagemRequestDTO;
import com.particaolar.mundo.system.dto.response.HospedagemResponseDTO;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import com.particaolar.mundo.system.exception.HospedagemNotFoundException;
import com.particaolar.mundo.system.exception.PetJaHospedadoException;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.mapper.HospedagemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class HospedagemService {

    private final HospedagemRepository hospedagemRepository;
    private final PetRepository petRepository;
    private final HospedagemMapper hospedagemMapper;

    // Valid status transitions
    private static final Set<StatusHospedagem> ACTIVE_STATUSES =
            Set.of(StatusHospedagem.AGENDADA, StatusHospedagem.EM_ANDAMENTO);

    @Transactional
    public HospedagemResponseDTO salvar(HospedagemRequestDTO dto) {
        if (dto.dataSaida() != null && dto.dataEntrada() != null
                && !dto.dataSaida().isAfter(dto.dataEntrada())) {
            throw new IllegalArgumentException("A data de saída deve ser posterior à data de entrada");
        }

        Pet pet = petRepository.findByIdAndAtivoTrue(dto.petId())
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
    public Page<HospedagemResponseDTO> listarTodas(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return hospedagemRepository.findAll(pageable)
                .map(hospedagemMapper::toResponseDTO);
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

        validateStatusTransition(hospedagem.getStatus(), novoStatus);
        hospedagem.setStatus(novoStatus);
        log.info("Status da hospedagem {} atualizado para {}", id, novoStatus);
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    @Transactional
    public HospedagemResponseDTO atualizar(Long id, HospedagemRequestDTO dto) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));

        if (hospedagem.getStatus() == StatusHospedagem.CANCELADA
                || hospedagem.getStatus() == StatusHospedagem.CONCLUIDA) {
            throw new IllegalStateException(
                    "Não é possível atualizar uma hospedagem " + hospedagem.getStatus());
        }

        if (dto.dataSaida() != null && dto.dataEntrada() != null
                && !dto.dataSaida().isAfter(dto.dataEntrada())) {
            throw new IllegalArgumentException("A data de saída deve ser posterior à data de entrada");
        }

        hospedagemMapper.updateEntityFromDTO(dto, hospedagem);
        log.info("Hospedagem {} atualizada", id);
        return hospedagemMapper.toResponseDTO(hospedagem);
    }

    @Transactional
    public void cancelar(Long id) {
        Hospedagem hospedagem = hospedagemRepository.findById(id)
                .orElseThrow(() -> new HospedagemNotFoundException(id));

        if (hospedagem.getStatus() == StatusHospedagem.CANCELADA) {
            throw new IllegalStateException("Hospedagem já está cancelada");
        }
        if (hospedagem.getStatus() == StatusHospedagem.CONCLUIDA) {
            throw new IllegalStateException("Não é possível cancelar uma hospedagem concluída");
        }

        hospedagem.setStatus(StatusHospedagem.CANCELADA);
        log.info("Hospedagem {} cancelada", id);
    }

    private void validateStatusTransition(StatusHospedagem current, StatusHospedagem next) {
        if (current == next) return;

        boolean valid = switch (current) {
            case AGENDADA -> next == StatusHospedagem.EM_ANDAMENTO
                    || next == StatusHospedagem.CANCELADA;
            case EM_ANDAMENTO -> next == StatusHospedagem.CONCLUIDA
                    || next == StatusHospedagem.CANCELADA;
            case CONCLUIDA, CANCELADA -> false;
        };

        if (!valid) {
            throw new IllegalStateException(
                    "Transição de status inválida: " + current + " -> " + next);
        }
    }
}