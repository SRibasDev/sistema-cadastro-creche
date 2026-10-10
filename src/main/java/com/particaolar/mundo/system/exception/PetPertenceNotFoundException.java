package com.particaolar.mundo.system.exception;

public class PetPertenceNotFoundException extends RuntimeException {

    public PetPertenceNotFoundException(Long id) {
        super("Pertence com ID " + id + " não encontrado.");
    }
}
