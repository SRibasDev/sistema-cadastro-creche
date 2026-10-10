package com.particaolar.mundo.system.exception;

public class ServicoNotFoundException extends RuntimeException {

    public ServicoNotFoundException(Long id) {
        super("Serviço com ID " + id + " não encontrado.");
    }
}
