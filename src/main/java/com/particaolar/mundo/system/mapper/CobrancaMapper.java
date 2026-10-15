package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Cobranca;
import com.particaolar.mundo.system.domain.entity.Pagamento;
import com.particaolar.mundo.system.dto.request.CobrancaRequestDTO;
import com.particaolar.mundo.system.dto.response.CobrancaResponseDTO;
import com.particaolar.mundo.system.dto.response.PagamentoResponseDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

@Component
public class CobrancaMapper {

    public Cobranca toEntity(@NotNull CobrancaRequestDTO dto) {
        Cobranca c = new Cobranca();
        c.setDescricao(dto.descricao().trim());
        c.setValorTotal(dto.valorTotal());
        c.setDesconto(dto.desconto() != null ? dto.desconto() : BigDecimal.ZERO);
        c.setValorCobrado(c.getValorTotal().subtract(c.getDesconto()));
        c.setValorPago(BigDecimal.ZERO);
        c.setDataVencimento(dto.dataVencimento());
        c.setObservacoes(dto.observacoes() != null ? dto.observacoes().trim() : null);
        return c;
    }

    public CobrancaResponseDTO toResponseDTO(@NotNull Cobranca c) {
        List<PagamentoResponseDTO> pagamentosDTO = c.getPagamentos() != null
                ? c.getPagamentos().stream().map(this::toPagamentoResponseDTO).toList()
                : Collections.emptyList();

        return new CobrancaResponseDTO(
                c.getId(),
                c.getTutor() != null ? c.getTutor().getId() : null,
                c.getTutor() != null ? c.getTutor().getNome() : null,
                c.getHospedagem() != null ? c.getHospedagem().getId() : null,
                c.getDescricao(),
                c.getValorTotal(),
                c.getDesconto(),
                c.getValorCobrado(),
                c.getValorPago(),
                c.getSaldoDevedor(),
                c.getDataVencimento(),
                c.getDataEmissao(),
                c.getStatus(),
                c.getObservacoes(),
                pagamentosDTO
        );
    }

    public PagamentoResponseDTO toPagamentoResponseDTO(@NotNull Pagamento p) {
        return new PagamentoResponseDTO(
                p.getId(),
                p.getCobranca() != null ? p.getCobranca().getId() : null,
                p.getValor(),
                p.getDataPagamento(),
                p.getFormaPagamento(),
                p.getComprovante(),
                p.getObservacoes(),
                p.getRegistradoPor()
        );
    }
}
