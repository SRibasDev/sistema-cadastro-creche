package com.particaolar.mundo.system.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Registro de FUNCIONARIO. Deve ser usado apenas por um ADMIN autenticado
 * (regra em SecurityConfig); o perfil do novo usuário é sempre FUNCIONARIO.
 */
public record RegisterRequestDTO(
        @NotBlank @Size(max = 100)
        String nome,

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, max = 100, message = "Senha deve ter no mínimo 8 caracteres")
        String senha
) {}