package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Cobranca;
import com.particaolar.mundo.system.dto.response.CobrancaResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class CobrancaMapper {

    public CobrancaResponseDTO toResponseDTO(Cobranca cobranca) {
        return new CobrancaResponseDTO(
                cobranca.getId(),
                cobranca.getOrigem(),
                cobranca.getReserva() != null ? cobranca.getReserva().getId() : null,
                cobranca.getPacoteComprado() != null ? cobranca.getPacoteComprado().getId() : null,
                cobranca.getPet().getId(),
                cobranca.getPet().getNome(),
                cobranca.getTutor().getId(),
                cobranca.getTutor().getNome(),
                cobranca.getDescricao(),
                cobranca.getValor(),
                cobranca.getDesconto() != null ? cobranca.getDesconto().getId() : null,
                cobranca.getDesconto() != null ? cobranca.getDesconto().getCodigo() : null,
                cobranca.getValorFinal(),
                cobranca.getDataVencimento(),
                cobranca.getStatus(),
                cobranca.getDataPagamento(),
                cobranca.getObservacoes(),
                cobranca.getCriadoEm()
        );
    }
}
