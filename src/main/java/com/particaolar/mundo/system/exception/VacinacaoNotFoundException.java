package com.particaolar.mundo.system.exception;

public class VacinacaoNotFoundException extends RuntimeException {
    public VacinacaoNotFoundException(Long id) {
        super("Vacinação não encontrada com o id: " + id);
    }
}
