package com.particaolar.mundo.system.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
        @NotBlank @Email
        String email,
        @NotBlank
        String senha
) {}