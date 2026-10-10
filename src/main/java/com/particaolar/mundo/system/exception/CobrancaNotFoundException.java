package com.particaolar.mundo.system.exception;

public class CobrancaNotFoundException extends RuntimeException {

    public CobrancaNotFoundException(Long id) {
        super("Cobrança com ID " + id + " não encontrada.");
    }
}
