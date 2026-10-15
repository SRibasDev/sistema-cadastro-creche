package com.particaolar.mundo.system.exception;

public class CobrancaNotFoundException extends RuntimeException {
    public CobrancaNotFoundException(Long id) {
        super("Cobrança não encontrada com ID: " + id);
    }
}
