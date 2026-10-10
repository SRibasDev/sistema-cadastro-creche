package com.particaolar.mundo.system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF;

public record TutorRequestDTO(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 100, message = "Nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "Telefone é obrigatório")
        @Pattern(regexp = "^\\d{10,11}$|^\\(?\\d{2}\\)?\\s?\\d{4,5}-?\\d{4}$",
                message = "Telefone deve ter 10 ou 11 dígitos (com ou sem máscara)")
        String telefone,

        @NotBlank(message = "CPF é obrigatório")
        @CPF(message = "CPF inválido")
        String cpf

) {}
