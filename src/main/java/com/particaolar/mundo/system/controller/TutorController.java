package com.particaolar.mundo.system.controller;

import com.particaolar.mundo.system.domain.entity.Tutor;
import com.particaolar.mundo.system.domain.service.TutorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tutores") // Esta é a porta de entrada (a URL)
@RequiredArgsConstructor
public class TutorController {

    // O Garçom (Controller) chama o Chef (Service)
    private final TutorService tutorService;

    // Endpoint 1: CADASTRAR UM TUTOR (Chega um pedido POST)
    @PostMapping
    public ResponseEntity<Tutor> cadastrar(@RequestBody Tutor tutor) {
        Tutor tutorSalvo = tutorService.salvarTutor(tutor);
        // Devolve o status 201 (Criado com Sucesso) e os dados do tutor gerado
        return ResponseEntity.status(HttpStatus.CREATED).body(tutorSalvo);
    }

    // Endpoint 2: LISTAR TODOS OS TUTORES (Chega um pedido GET)
    @GetMapping
    public ResponseEntity<List<Tutor>> listarTodos() {
        List<Tutor> tutores = tutorService.listarTodos();
        // Devolve o status 200 (OK) e a lista
        return ResponseEntity.ok(tutores);
    }

    // Endpoint 3: BUSCAR TUTOR POR ID (Chega um pedido GET com o número na URL)
    @GetMapping("/{id}")
    public ResponseEntity<Tutor> buscarPorId(@PathVariable Long id) {
        Tutor tutor = tutorService.buscarPorId(id);
        return ResponseEntity.ok(tutor);
    }
}