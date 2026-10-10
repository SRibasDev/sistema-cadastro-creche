package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Desconto;
import com.particaolar.mundo.system.dto.request.DescontoRequestDTO;
import com.particaolar.mundo.system.dto.response.DescontoResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class DescontoMapper {

    public Desconto toEntity(DescontoRequestDTO dto) {
        Desconto desconto = new Desconto();
        desconto.setCodigo(dto.codigo());
        desconto.setNome(dto.nome());
        desconto.setTipo(dto.tipo());
        desconto.setValor(dto.valor());
        desconto.setValidade(dto.validade());
        return desconto;
    }

    public DescontoResponseDTO toResponseDTO(Desconto desconto) {
        return new DescontoResponseDTO(
                desconto.getId(),
                desconto.getCodigo(),
                desconto.getNome(),
                desconto.getTipo(),
                desconto.getValor(),
                desconto.getValidade(),
                desconto.getAtivo(),
                desconto.getCriadoEm()
        );
    }

    public void updateEntityFromDTO(DescontoRequestDTO dto, Desconto desconto) {
        desconto.setCodigo(dto.codigo());
        desconto.setNome(dto.nome());
        desconto.setTipo(dto.tipo());
        desconto.setValor(dto.valor());
        desconto.setValidade(dto.validade());
    }
}
