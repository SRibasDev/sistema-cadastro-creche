package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Pacote;
import com.particaolar.mundo.system.domain.entity.PacoteComprado;
import com.particaolar.mundo.system.dto.request.PacoteRequestDTO;
import com.particaolar.mundo.system.dto.response.PacoteCompradoResponseDTO;
import com.particaolar.mundo.system.dto.response.PacoteResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class PacoteMapper {

    public Pacote toEntity(PacoteRequestDTO dto) {
        Pacote pacote = new Pacote();
        pacote.setNome(dto.nome());
        pacote.setDescricao(dto.descricao());
        pacote.setQuantidade(dto.quantidade());
        pacote.setPrecoTotal(dto.precoTotal());
        pacote.setValidadeDias(dto.validadeDias());
        return pacote;
    }

    public PacoteResponseDTO toResponseDTO(Pacote pacote) {
        return new PacoteResponseDTO(
                pacote.getId(),
                pacote.getNome(),
                pacote.getDescricao(),
                pacote.getServico().getId(),
                pacote.getServico().getNome(),
                pacote.getServico().getUnidade(),
                pacote.getQuantidade(),
                pacote.getPrecoTotal(),
                pacote.getValidadeDias(),
                pacote.getDesconto() != null ? pacote.getDesconto().getId() : null,
                pacote.getDesconto() != null ? pacote.getDesconto().getCodigo() : null,
                pacote.getAtivo(),
                pacote.getCriadoEm()
        );
    }

    public void updateEntityFromDTO(PacoteRequestDTO dto, Pacote pacote) {
        pacote.setNome(dto.nome());
        pacote.setDescricao(dto.descricao());
        pacote.setQuantidade(dto.quantidade());
        pacote.setPrecoTotal(dto.precoTotal());
        pacote.setValidadeDias(dto.validadeDias());
    }

    public PacoteCompradoResponseDTO toCompradoResponseDTO(PacoteComprado comprado) {
        return new PacoteCompradoResponseDTO(
                comprado.getId(),
                comprado.getPacote().getId(),
                comprado.getPacote().getNome(),
                comprado.getPacote().getServico().getUnidade(),
                comprado.getPet().getId(),
                comprado.getPet().getNome(),
                comprado.getSaldo(),
                comprado.getValorPago(),
                comprado.getValidade(),
                comprado.getAtivo(),
                comprado.getCriadoEm()
        );
    }
}
