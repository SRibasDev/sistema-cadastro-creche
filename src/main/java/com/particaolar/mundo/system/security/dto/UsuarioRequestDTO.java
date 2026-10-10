package com.particaolar.mundo.system.security.dto;

import com.particaolar.mundo.system.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Criação de usuário com perfil específico. Restrito a ADMIN.
 */
public record UsuarioRequestDTO(
        @NotBlank @Size(max = 100)
        String nome,

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 100, message = "Senha deve ter no mínimo 8 caracteres")
        String senha,

        @NotNull
        Role role
) {}