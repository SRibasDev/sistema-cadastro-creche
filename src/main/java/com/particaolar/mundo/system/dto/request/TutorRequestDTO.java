package com.particaolar.mundo.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record TutorRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        // Aceita com ou sem máscara. A normalização para 10–11 dígitos é
        // garantida pelo NormalizadorDados antes de persistir/verificar unicidade.
        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(
                regexp = "^\\(?(\\d{2})\\)?[\\s.-]?(\\d{4,5})[\\s.-]?(\\d{4})$",
                message = "Telefone inválido. Use DDD + número (10 ou 11 dígitos)."
        )
        String telefone,

        // Aceita com ou sem máscara; o @CPF valida os dígitos verificadores.
        // Persistência e unicidade usam a forma normalizada (só dígitos).
        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf

) {}
