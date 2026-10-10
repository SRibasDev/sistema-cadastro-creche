package com.particaolar.mundo.system.exception;

public class PacoteNotFoundException extends RuntimeException {

    public PacoteNotFoundException(Long id) {
        super("Pacote com ID " + id + " não encontrado.");
    }
}
