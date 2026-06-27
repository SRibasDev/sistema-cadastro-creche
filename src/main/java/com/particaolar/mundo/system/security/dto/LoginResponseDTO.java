package com.particaolar.mundo.system.security.dto;

public record LoginResponseDTO(
        String token,
        String nome,
        String role
) {}