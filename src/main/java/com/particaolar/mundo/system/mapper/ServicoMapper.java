package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Servico;
import com.particaolar.mundo.system.dto.request.ServicoRequestDTO;
import com.particaolar.mundo.system.dto.response.ServicoResponseDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

    public Servico toEntity(@NotNull ServicoRequestDTO dto) {
        Servico s = new Servico();
        s.setNome(dto.nome().trim());
        s.setDescricao(dto.descricao() != null ? dto.descricao().trim() : null);
        s.setTipo(dto.tipo());
        s.setPreco(dto.preco());
        s.setUnidade(dto.unidade());
        return s;
    }

    public ServicoResponseDTO toResponseDTO(@NotNull Servico s) {
        return new ServicoResponseDTO(
                s.getId(),
                s.getNome(),
                s.getDescricao(),
                s.getTipo(),
                s.getPreco(),
                s.getUnidade(),
                s.getAtivo(),
                s.getCriadoEm(),
                s.getAtualizadoEm()
        );
    }

    public void updateEntityFromDTO(@NotNull ServicoRequestDTO dto, @NotNull Servico s) {
        s.setNome(dto.nome().trim());
        s.setDescricao(dto.descricao() != null ? dto.descricao().trim() : null);
        s.setTipo(dto.tipo());
        s.setPreco(dto.preco());
        s.setUnidade(dto.unidade());
    }
}
