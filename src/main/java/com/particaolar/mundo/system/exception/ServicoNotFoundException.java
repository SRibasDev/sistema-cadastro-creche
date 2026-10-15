package com.particaolar.mundo.system.exception;

public class ServicoNotFoundException extends RuntimeException {
    public ServicoNotFoundException(Long id) {
        super("Serviço não encontrado com ID: " + id);
    }
}
