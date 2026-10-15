package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Pet;
import com.particaolar.mundo.system.domain.entity.Vacinacao;
import com.particaolar.mundo.system.domain.repository.PetRepository;
import com.particaolar.mundo.system.domain.repository.VacinacaoRepository;
import com.particaolar.mundo.system.dto.request.VacinacaoRequestDTO;
import com.particaolar.mundo.system.dto.response.VacinacaoResponseDTO;
import com.particaolar.mundo.system.exception.PetNotFoundException;
import com.particaolar.mundo.system.exception.VacinacaoNotFoundException;
import com.particaolar.mundo.system.mapper.VacinacaoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VacinacaoService {

    private static final int DIAS_ALERTA_VENCIMENTO = 30;

    private final VacinacaoRepository vacinacaoRepository;
    private final PetRepository petRepository;
    private final VacinacaoMapper vacinacaoMapper;

    @Autowired(required = false)
    private AuditLogService auditLogService;

    @Transactional
    public VacinacaoResponseDTO registrar(Long petId, VacinacaoRequestDTO dto) {
        Pet pet = petRepository.findByIdAndAtivoTrue(petId)
                .orElseThrow(() -> new PetNotFoundException(petId));
        validarDatas(dto);

        boolean duplicada = vacinacaoRepository.findByPetIdAndAtivoTrueOrderByDataVencimentoAsc(petId).stream()
                .anyMatch(v -> v.getNomeVacina().equalsIgnoreCase(dto.nomeVacina().trim())
                        && v.getDataAplicacao().equals(dto.dataAplicacao()));
        if (duplicada) {
            throw new IllegalArgumentException("Ja existe um registro ativo de '" + dto.nomeVacina().trim()
                    + "' aplicado em " + dto.dataAplicacao() + " para este pet.");
        }

        Vacinacao entity = vacinacaoMapper.toEntity(dto);
        entity.setPet(pet);
        Vacinacao salva = vacinacaoRepository.save(entity);
        log.info("Vacinacao registrada id={} petId={} vacina={}", salva.getId(), petId, salva.getNomeVacina());
        if (auditLogService != null) {
            auditLogService.registrar(null, "REGISTRAR_VACINACAO", "VACINACAO", salva.getId(),
                    "Pet: " + pet.getNome() + " Vacina: " + salva.getNomeVacina()
                            + " Vencimento: " + salva.getDataVencimento());
        }
        return vacinacaoMapper.toResponseDTO(salva);
    }

    @Transactional
    public VacinacaoResponseDTO atualizar(Long id, VacinacaoRequestDTO dto) {
        Vacinacao entity = vacinacaoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VacinacaoNotFoundException(id));
        validarDatas(dto);
        vacinacaoMapper.updateEntityFromDTO(dto, entity);
        Vacinacao atualizada = vacinacaoRepository.save(entity);
        log.info("Vacinacao atualizada id={}", id);
        if (auditLogService != null) {
            auditLogService.registrar(null, "ATUALIZAR_VACINACAO", "VACINACAO", id,
                    "Vacina: " + atualizada.getNomeVacina() + " Vencimento: " + atualizada.getDataVencimento());
        }
        return vacinacaoMapper.toResponseDTO(atualizada);
    }

    @Transactional(readOnly = true)
    public VacinacaoResponseDTO buscarPorId(Long id) {
        Vacinacao entity = vacinacaoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VacinacaoNotFoundException(id));
        return vacinacaoMapper.toResponseDTO(entity);
    }

    @Transactional(readOnly = true)
    public List<VacinacaoResponseDTO> listarDoPet(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new PetNotFoundException(petId);
        }
        return vacinacaoRepository.findByPetIdAndAtivoTrueOrderByDataVencimentoAsc(petId).stream()
                .map(vacinacaoMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<VacinacaoResponseDTO> listarDoPetPaginado(Long petId, Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        if (!petRepository.existsById(petId)) {
            throw new PetNotFoundException(petId);
        }
        return vacinacaoRepository.findByPetIdAndAtivoTrue(petId, pageable)
                .map(vacinacaoMapper::toResponseDTO);
    }

    /** Vacinas já vencidas (ativas) de todos os pets, ordenadas pelo vencimento. */
    @Transactional(readOnly = true)
    public List<VacinacaoResponseDTO> listarVencidas() {
        return vacinacaoRepository.buscarVencidas(LocalDate.now()).stream()
                .map(vacinacaoMapper::toResponseDTO)
                .toList();
    }

    /** Vacinas que vencem nos próximos 30 dias (ativas). */
    @Transactional(readOnly = true)
    public List<VacinacaoResponseDTO> listarAVencer() {
        LocalDate hoje = LocalDate.now();
        return vacinacaoRepository.buscarAVencer(hoje, hoje.plusDays(DIAS_ALERTA_VENCIMENTO)).stream()
                .map(vacinacaoMapper::toResponseDTO)
                .toList();
    }

    /** Vacinas vencidas (ativas) de um pet específico. */
    @Transactional(readOnly = true)
    public List<VacinacaoResponseDTO> listarVencidasDoPet(Long petId) {
        if (!petRepository.existsById(petId)) {
            throw new PetNotFoundException(petId);
        }
        return vacinacaoRepository.buscarVencidasDoPet(petId, LocalDate.now()).stream()
                .map(vacinacaoMapper::toResponseDTO)
                .toList();
    }

    /** Indica se o pet possui alguma vacina vencida (útil para alertas no check-in). */
    @Transactional(readOnly = true)
    public boolean petPossuiVacinaVencida(Long petId) {
        return !vacinacaoRepository.buscarVencidasDoPet(petId, LocalDate.now()).isEmpty();
    }

    @Transactional
    public void remover(Long id) {
        Vacinacao entity = vacinacaoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new VacinacaoNotFoundException(id));
        entity.setAtivo(false);
        vacinacaoRepository.save(entity);
        log.info("Vacinacao desativada: id={}", id);
        if (auditLogService != null) {
            auditLogService.registrar(null, "REMOVER_VACINACAO", "VACINACAO", id,
                    "Vacina: " + entity.getNomeVacina());
        }
    }

    private void validarDatas(VacinacaoRequestDTO dto) {
        if (dto.dataVencimento().isBefore(dto.dataAplicacao())) {
            throw new IllegalArgumentException("A data de vencimento deve ser posterior a data de aplicacao.");
        }
        if (dto.dataAplicacao().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("A data de aplicacao nao pode ser no futuro.");
        }
    }
}
