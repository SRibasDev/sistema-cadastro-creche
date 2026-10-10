package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Servico;
import com.particaolar.mundo.system.domain.repository.ServicoRepository;
import com.particaolar.mundo.system.dto.request.ServicoRequestDTO;
import com.particaolar.mundo.system.dto.response.ServicoResponseDTO;
import com.particaolar.mundo.system.exception.ServicoNotFoundException;
import com.particaolar.mundo.system.mapper.ServicoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Catálogo de serviços com preço. Escrita: ADMIN. Leitura: ambos os perfis.
 * SaaS: o catálogo futuramente será por tenant (filtro no repository).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final ServicoMapper servicoMapper;

    @Transactional
    public ServicoResponseDTO salvar(ServicoRequestDTO dto) {
        if (servicoRepository.existsByNome(dto.nome())) {
            throw new IllegalStateException("Já existe um serviço com o nome '" + dto.nome() + "'");
        }
        Servico servico = servicoMapper.toEntity(dto);
        Servico salvo = servicoRepository.save(servico);
        log.info("Serviço criado: {} (R$ {})", salvo.getNome(), salvo.getPreco());
        return servicoMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<ServicoResponseDTO> listarTodos(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return servicoRepository.findByAtivoTrue(pageable).map(servicoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public ServicoResponseDTO buscarPorId(Long id) {
        Servico servico = servicoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ServicoNotFoundException(id));
        return servicoMapper.toResponseDTO(servico);
    }

    @Transactional
    public ServicoResponseDTO atualizar(Long id, ServicoRequestDTO dto) {
        Servico servico = servicoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ServicoNotFoundException(id));
        servicoMapper.updateEntityFromDTO(dto, servico);
        log.info("Serviço {} atualizado", id);
        return servicoMapper.toResponseDTO(servico);
    }

    @Transactional
    public void deletar(Long id) {
        Servico servico = servicoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ServicoNotFoundException(id));
        servico.setAtivo(false);
        log.info("Serviço {} desativado", id);
    }

    /** Usado por PacoteService. */
    public Servico buscarEntidade(Long id) {
        return servicoRepository.findByIdAndAtivoTrue(id)
                .orElseThrow(() -> new ServicoNotFoundException(id));
    }
}
