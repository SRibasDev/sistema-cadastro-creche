package com.particaolar.mundo.system.exception;

public class TutorCpfAlreadyExistsException extends RuntimeException {
    public TutorCpfAlreadyExistsException(String cpf) {
        super("Já existe um tutor cadastrado com o CPF " + cpf);
    }
}
