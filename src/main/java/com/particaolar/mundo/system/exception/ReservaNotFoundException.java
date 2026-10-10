package com.particaolar.mundo.system.exception;

public class ReservaNotFoundException extends RuntimeException {

    public ReservaNotFoundException(Long id) {
        super("Reserva com ID " + id + " não encontrada.");
    }
}
