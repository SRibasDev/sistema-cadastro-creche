package com.particaolar.mundo.system.exception;

public class TutorPhoneNumberAlreadyExistsException extends RuntimeException {
    public TutorPhoneNumberAlreadyExistsException(String telefone) {
        super("Já existe um tutor cadastrado com o telefone: " + telefone);
    }
}
