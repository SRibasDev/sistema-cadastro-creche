package com.particaolar.mundo.system.dto.request;

import com.particaolar.mundo.system.enums.TipoDesconto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DescontoRequestDTO(

        @NotBlank(message = "Código é obrigatório")
        @Size(max = 30, message = "Código deve ter no máximo 30 caracteres")
        String codigo,

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 80, message = "Nome deve ter no máximo 80 caracteres")
        String nome,

        @NotNull(message = "Tipo é obrigatório")
        TipoDesconto tipo,

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.00", inclusive = false, message = "Valor deve ser maior que zero")
        BigDecimal valor,

        LocalDate validade
) {}
