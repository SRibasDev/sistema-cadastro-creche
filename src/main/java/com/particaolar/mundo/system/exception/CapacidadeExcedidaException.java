package com.particaolar.mundo.system.exception;

import java.time.LocalDate;

public class CapacidadeExcedidaException extends RuntimeException {

    public CapacidadeExcedidaException(LocalDate dia, int capacidade) {
        super("Capacidade máxima atingida em " + dia + " (vagas: " + capacidade + ")");
    }
}
