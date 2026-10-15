package com.particaolar.mundo.system.security.dto;

import com.particaolar.mundo.system.enums.Role;
import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        Role role,
        Boolean ativo,
        LocalDateTime criadoEm
) {}
