package com.particaolar.mundo.system.exception;

import com.particaolar.mundo.system.enums.Modalidade;

import java.time.LocalDate;

/**
 * A capacidade configurada para uma dada data e modalidade foi excedida.
 * Resposta HTTP 409 (Conflito).
 */
public class CapacidadeExcedidaException extends RuntimeException {
    public CapacidadeExcedidaException(LocalDate dia, Modalidade modalidade) {
        super("Capacidade de " + modalidade + " atingida em " + dia + ". Escolha outra data.");
    }
}
