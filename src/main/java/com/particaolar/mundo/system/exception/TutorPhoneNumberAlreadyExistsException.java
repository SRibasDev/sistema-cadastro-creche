package com.particaolar.mundo.system.exception;

public class TutorPhoneNumberAlreadyExistsException extends RuntimeException {
    public TutorPhoneNumberAlreadyExistsException() {
        super("Já existe um tutor cadastrado com este telefone");
    }
}
