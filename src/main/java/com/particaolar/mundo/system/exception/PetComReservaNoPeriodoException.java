package com.particaolar.mundo.system.exception;

public class PetComReservaNoPeriodoException extends RuntimeException {

    public PetComReservaNoPeriodoException(Long petId) {
        super("O pet com ID " + petId + " já possui reserva ativa neste período.");
    }
}
