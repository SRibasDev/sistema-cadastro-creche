package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Servico;
import com.particaolar.mundo.system.domain.repository.ServicoRepository;
import com.particaolar.mundo.system.dto.request.ServicoRequestDTO;
import com.particaolar.mundo.system.dto.response.ServicoResponseDTO;
import com.particaolar.mundo.system.enums.TipoServico;
import com.particaolar.mundo.system.exception.ServicoNotFoundException;
import com.particaolar.mundo.system.mapper.ServicoMapper;
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
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private AuditLogService auditLogService;

    @Transactional
    public ServicoResponseDTO salvar(ServicoRequestDTO dto) {
        if (servicoRepository.existsByNomeIgnoreCase(dto.nome().trim())) {
            throw new IllegalArgumentException("Já existe um serviço cadastrado com o nome: " + dto.nome().trim());
        }
        Servico entity = servicoMapper.toEntity(dto);
        Servico salvo = servicoRepository.save(entity);
        log.info("Serviço criado com ID: {} ({})", salvo.getId(), salvo.getNome());
        if (auditLogService != null) {
            auditLogService.registrar(null, "CRIAR_SERVICO", "SERVICO", salvo.getId(), "Nome: " + salvo.getNome() + " Preço: R$ " + salvo.getPreco());
        }
        return servicoMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponseDTO> listarAtivos(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return servicoRepository.findByAtivoTrue(pageable)
                .map(servicoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> listarTodosAtivos() {
        return servicoRepository.findByAtivoTrue().stream()
                .map(servicoMapper::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponseDTO> listarTodos(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return servicoRepository.findAll(pageable)
                .map(servicoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ServicoResponseDTO buscarPorId(Long id) {
        Servico s = servicoRepository.findById(id)
                .orElseThrow(() -> new ServicoNotFoundException(id));
        return servicoMapper.toResponseDTO(s);
    }

    @Transactional(readOnly = true)
    public List<ServicoResponseDTO> buscarPorTipo(TipoServico tipo) {
        return servicoRepository.findByTipoAndAtivoTrue(tipo).stream()
                .map(servicoMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public ServicoResponseDTO atualizar(Long id, ServicoRequestDTO dto) {
        Servico s = servicoRepository.findById(id)
                .orElseThrow(() -> new ServicoNotFoundException(id));

        if (servicoRepository.existsByNomeIgnoreCaseAndIdNot(dto.nome().trim(), id)) {
            throw new IllegalArgumentException("Já existe outro serviço cadastrado com o nome: " + dto.nome().trim());
        }

        servicoMapper.updateEntityFromDTO(dto, s);
        Servico atualizado = servicoRepository.save(s);
        log.info("Serviço atualizado ID: {} ({})", atualizado.getId(), atualizado.getNome());
        if (auditLogService != null) {
            auditLogService.registrar(null, "ATUALIZAR_SERVICO", "SERVICO", atualizado.getId(), "Nome: " + atualizado.getNome() + " Preço: R$ " + atualizado.getPreco());
        }
        return servicoMapper.toResponseDTO(atualizado);
    }

    @Transactional
    public void alterarStatus(Long id, boolean ativo) {
        Servico s = servicoRepository.findById(id)
                .orElseThrow(() -> new ServicoNotFoundException(id));
        s.setAtivo(ativo);
        servicoRepository.save(s);
        log.info("Serviço ID: {} teve status ativo alterado para {}", id, ativo);
        if (auditLogService != null) {
            auditLogService.registrar(null, "ALTERAR_STATUS_SERVICO", "SERVICO", id, "Ativo: " + ativo);
        }
    }
}
