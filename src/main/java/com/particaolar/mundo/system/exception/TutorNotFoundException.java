package com.particaolar.mundo.system.exception;

import static org.springframework.data.jpa.domain.AbstractPersistable_.id;

public class TutorNotFoundException extends RuntimeException {
    public TutorNotFoundException(Long id) {
        super("Tutor não encontrado com o id: " + id);
    }
}
