package com.particaolar.mundo.system.domain.service;

import com.particaolar.mundo.system.domain.entity.Desconto;
import com.particaolar.mundo.system.domain.repository.DescontoRepository;
import com.particaolar.mundo.system.dto.request.DescontoRequestDTO;
import com.particaolar.mundo.system.dto.response.DescontoResponseDTO;
import com.particaolar.mundo.system.exception.DescontoNotFoundException;
import com.particaolar.mundo.system.mapper.DescontoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Descontos e cupons promocionais. Escrita: ADMIN. Leitura: ambos os perfis.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DescontoService {

    private final DescontoRepository descontoRepository;
    private final DescontoMapper descontoMapper;

    @Transactional
    public DescontoResponseDTO salvar(DescontoRequestDTO dto) {
        if (dto.tipo() == com.particaolar.mundo.system.enums.TipoDesconto.PERCENTUAL
                && dto.valor().compareTo(java.math.BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Desconto percentual não pode ser maior que 100%");
        }
        if (descontoRepository.existsByCodigoIgnoreCase(dto.codigo())) {
            throw new IllegalStateException("Já existe um desconto com o código '" + dto.codigo() + "'");
        }
        Desconto desconto = descontoMapper.toEntity(dto);
        Desconto salvo = descontoRepository.save(desconto);
        log.info("Desconto criado: {} ({})", salvo.getCodigo(), salvo.getTipo());
        return descontoMapper.toResponseDTO(salvo);
    }

    @Transactional(readOnly = true)
    public Page<DescontoResponseDTO> listarTodos(Pageable pageable) {
        if (pageable.getPageSize() > 100) {
            throw new IllegalArgumentException("Tamanho máximo de página é 100");
        }
        return descontoRepository.findAll(pageable).map(descontoMapper::toResponseDTO);
    }

    @Transactional(readOnly = true)
    public DescontoResponseDTO buscarPorId(Long id) {
        return descontoMapper.toResponseDTO(buscarEntidade(id));
    }

    @Transactional
    public DescontoResponseDTO atualizar(Long id, DescontoRequestDTO dto) {
        Desconto desconto = buscarEntidade(id);
        descontoMapper.updateEntityFromDTO(dto, desconto);
        log.info("Desconto {} atualizado", id);
        return descontoMapper.toResponseDTO(desconto);
    }

    @Transactional
    public void deletar(Long id) {
        Desconto desconto = buscarEntidade(id);
        desconto.setAtivo(false);
        log.info("Desconto {} desativado", id);
    }

    Desconto buscarEntidade(Long id) {
        return descontoRepository.findById(id)
                .orElseThrow(() -> new DescontoNotFoundException(id));
    }
}
