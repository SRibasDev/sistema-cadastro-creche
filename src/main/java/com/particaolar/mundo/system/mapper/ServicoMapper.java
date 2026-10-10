package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Servico;
import com.particaolar.mundo.system.dto.request.ServicoRequestDTO;
import com.particaolar.mundo.system.dto.response.ServicoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

    public Servico toEntity(ServicoRequestDTO dto) {
        Servico servico = new Servico();
        servico.setNome(dto.nome());
        servico.setDescricao(dto.descricao());
        servico.setPreco(dto.preco());
        servico.setUnidade(dto.unidade());
        return servico;
    }

    public ServicoResponseDTO toResponseDTO(Servico servico) {
        return new ServicoResponseDTO(
                servico.getId(),
                servico.getNome(),
                servico.getDescricao(),
                servico.getPreco(),
                servico.getUnidade(),
                servico.getAtivo(),
                servico.getCriadoEm()
        );
    }

    public void updateEntityFromDTO(ServicoRequestDTO dto, Servico servico) {
        servico.setNome(dto.nome());
        servico.setDescricao(dto.descricao());
        servico.setPreco(dto.preco());
        servico.setUnidade(dto.unidade());
    }
}
