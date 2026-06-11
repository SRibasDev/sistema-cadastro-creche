package com.particaolar.mundo.system.exception;

public class PetNotFoundException extends RuntimeException {
    public PetNotFoundException(Long id) {
        super("Pet não encontrado com o id: " + id);
    }
}
