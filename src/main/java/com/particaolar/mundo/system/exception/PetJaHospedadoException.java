package com.particaolar.mundo.system.exception;

public class PetJaHospedadoException extends RuntimeException {
    public PetJaHospedadoException(Long petId) {
        super("O pet com ID " + petId + " já possui uma hospedagem ativa.");
    }
}
