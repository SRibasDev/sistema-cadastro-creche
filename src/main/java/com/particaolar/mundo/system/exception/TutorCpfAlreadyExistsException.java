package com.particaolar.mundo.system.exception;

public class TutorCpfAlreadyExistsException extends RuntimeException {
    public TutorCpfAlreadyExistsException() {
        super("Já existe um tutor cadastrado com este CPF");
    }
}
