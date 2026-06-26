package com.particaolar.mundo.system.exception;

public class TutorNotFoundException extends RuntimeException {
    public TutorNotFoundException(Long id) {
        super("Tutor não encontrado com o id: " + id);
    }
}
