package com.particaolar.mundo.system.mapper;

import com.particaolar.mundo.system.domain.entity.Vacinacao;
import com.particaolar.mundo.system.dto.request.VacinacaoRequestDTO;
import com.particaolar.mundo.system.dto.response.VacinacaoResponseDTO;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class VacinacaoMapper {

    public Vacinacao toEntity(@NotNull VacinacaoRequestDTO dto) {
        Vacinacao entity = new Vacinacao();
        updateEntityFromDTO(dto, entity);
        return entity;
    }

    public void updateEntityFromDTO(@NotNull VacinacaoRequestDTO dto, @NotNull Vacinacao entity) {
        entity.setNomeVacina(dto.nomeVacina().trim());
        entity.setDataAplicacao(dto.dataAplicacao());
        entity.setDataVencimento(dto.dataVencimento());
        entity.setLote(dto.lote() != null && !dto.lote().isBlank() ? dto.lote().trim() : null);
        entity.setVeterinario(dto.veterinario() != null && !dto.veterinario().isBlank() ? dto.veterinario().trim() : null);
        entity.setObservacoes(dto.observacoes() != null && !dto.observacoes().isBlank() ? dto.observacoes().trim() : null);
    }

    public VacinacaoResponseDTO toResponseDTO(@NotNull Vacinacao entity) {
        return new VacinacaoResponseDTO(
                entity.getId(),
                entity.getPet() != null ? entity.getPet().getId() : null,
                entity.getPet() != null ? entity.getPet().getNome() : null,
                entity.getNomeVacina(),
                entity.getDataAplicacao(),
                entity.getDataVencimento(),
                entity.getLote(),
                entity.getVeterinario(),
                entity.getObservacoes(),
                entity.getSituacao(),
                entity.getAtivo(),
                entity.getCriadoEm()
        );
    }
}
