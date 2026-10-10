package com.particaolar.mundo.system.exception;

public class DescontoNotFoundException extends RuntimeException {

    public DescontoNotFoundException(Long id) {
        super("Desconto com ID " + id + " não encontrado.");
    }
}
