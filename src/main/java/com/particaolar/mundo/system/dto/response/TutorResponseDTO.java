package com.particaolar.mundo.system.dto.response;

import java.time.LocalDateTime;

public record TutorResponseDTO(
        Long id,
        String nome,
        String telefone,
        String cpf,
        String email,
        String observacoes,
        Boolean ativo,
        LocalDateTime criadoEm
) {
    public TutorResponseDTO(Long id, String nome, String telefone) {
        this(id, nome, telefone, null, null, null, true, null);
    }
}
