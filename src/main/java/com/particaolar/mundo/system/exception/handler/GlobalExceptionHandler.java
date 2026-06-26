package com.particaolar.mundo.system.exception.handler;

import com.particaolar.mundo.system.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            PetNotFoundException.class,
            TutorNotFoundException.class,
            HospedagemNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(
                        404,
                        "Recurso não encontrado",
                        e.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler({
            TutorCpfAlreadyExistsException.class,
            TutorPhoneNumberAlreadyExistsException.class,
            PetJaHospedadoException.class
    })
    public ResponseEntity<ErrorResponse> handleConflict(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(
                        409,
                        "Conflito de dados",
                        e.getMessage(),
                        LocalDateTime.now()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(
                        500,
                        "Erro interno no servidor",
                        e.getMessage(),
                        LocalDateTime.now()
                ));
    }
}