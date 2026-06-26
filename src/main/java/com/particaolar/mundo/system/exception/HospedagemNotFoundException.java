package com.particaolar.mundo.system.exception;

public class HospedagemNotFoundException extends RuntimeException {
    public HospedagemNotFoundException(Long id) {
        super("Hospedagem com ID " + id + " não encontrada.");
    }
}
