package com.particaolar.mundo.system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({PetNotFoundException.class, TutorNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        Map<String, Object> erro = new HashMap<>();
        erro.put("status", 404);
        erro.put("erro", "Recurso não encontrado");
        erro.put("mensagem", ex.getMessage());
        erro.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex) {
        Map<String, Object> erro = new HashMap<>();
        erro.put("status",400);
        erro.put("erro","Erro de negócio");
        erro.put("mensagem", ex.getMessage());
        erro.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(erro);
    }

    @ExceptionHandler (Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex){
        Map<String,Object> erro = new HashMap<>();
        erro.put("status",500);
        erro.put("erro","Erro interno no servidor");
        erro.put("mensagem",ex.getMessage());
        erro.put("timestamp",LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);

    }
}
