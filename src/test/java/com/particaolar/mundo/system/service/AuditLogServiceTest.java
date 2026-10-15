package com.particaolar.mundo.system.service;

import com.particaolar.mundo.system.domain.entity.AuditLog;
import com.particaolar.mundo.system.domain.repository.AuditLogRepository;
import com.particaolar.mundo.system.domain.service.AuditLogService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve registrar log com usuário explícito")
    void registrar_comUsuarioExplicito() {
        auditLogService.registrar("admin@creche.com", "CANCELAR_RESERVA", "HOSPEDAGEM", 1L, "Motivo: Viagem cancelada");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog salvo = captor.getValue();
        assertEquals("admin@creche.com", salvo.getUsuario());
        assertEquals("CANCELAR_RESERVA", salvo.getAcao());
        assertEquals("HOSPEDAGEM", salvo.getEntidade());
        assertEquals(1L, salvo.getEntidadeId());
        assertEquals("Motivo: Viagem cancelada", salvo.getDetalhes());
        assertNotNull(salvo.getDataHora());
    }

    @Test
    @DisplayName("Deve registrar log obtendo usuário do SecurityContextHolder quando nulo")
    void registrar_comUsuarioDoContexto() {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("func@creche.com", "cred", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);

        auditLogService.registrar(null, "CHECK_IN", "HOSPEDAGEM", 5L, "Entrada realizada");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog salvo = captor.getValue();
        assertEquals("func@creche.com", salvo.getUsuario());
        assertEquals("CHECK_IN", salvo.getAcao());
    }

    @Test
    @DisplayName("Deve registrar SISTEMA como usuário se não autenticado")
    void registrar_semAutenticacao_usaSistema() {
        auditLogService.registrar(null, "BOOTSTRAP", "SISTEMA", null, "Bootstrap admin");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog salvo = captor.getValue();
        assertEquals("SISTEMA", salvo.getUsuario());
    }

    @Test
    @DisplayName("Não deve estourar exceção se save falhar")
    void registrar_falhaNoSave_naoPropagaExcecao() {
        doThrow(new RuntimeException("DB offline")).when(auditLogRepository).save(any());

        assertDoesNotThrow(() ->
                auditLogService.registrar("admin@creche.com", "TESTE", "ENTIDADE", 1L, "Detalhes")
        );
    }

    @Test
    @DisplayName("Deve listar logs paginados")
    void listarLogs_sucesso() {
        Pageable pageable = PageRequest.of(0, 10);
        AuditLog log1 = new AuditLog(1L, "admin@creche.com", "LOGIN", "AUTH", null, "OK", LocalDateTime.now());
        Page<AuditLog> pageMock = new PageImpl<>(List.of(log1), pageable, 1);

        when(auditLogRepository.findAllByOrderByDataHoraDesc(pageable)).thenReturn(pageMock);

        Page<AuditLog> resultado = auditLogService.listarLogs(pageable);
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("LOGIN", resultado.getContent().get(0).getAcao());
    }

    @Test
    @DisplayName("Deve rejeitar listagem com pageSize > 100")
    void listarLogs_pageSizeExcessivo_lancaExcecao() {
        Pageable pageable = PageRequest.of(0, 101);
        assertThrows(IllegalArgumentException.class, () -> auditLogService.listarLogs(pageable));
    }
}
