package com.particaolar.mundo.system.exception;

public class SaldoInsuficienteException extends RuntimeException {

    public SaldoInsuficienteException(Long pacoteCompradoId) {
        super("Saldo insuficiente no pacote comprado ID " + pacoteCompradoId);
    }
}
