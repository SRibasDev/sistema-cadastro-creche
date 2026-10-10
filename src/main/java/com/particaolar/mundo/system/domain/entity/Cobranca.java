package com.particaolar.mundo.system.domain.entity;

import com.particaolar.mundo.system.enums.OrigemCobranca;
import com.particaolar.mundo.system.enums.StatusCobranca;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Cobrança gerada por uma reserva ou pacote, com vencimento e status.
 * Pagamento registrado manualmente (sem gateway nesta fase).
 * SaaS: futuramente (tenant_id) — tutor/pet já são escopo do tenant.
 */
@Entity
@Table(name = "tb_cobrancas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemCobranca origem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pacote_comprado_id")
    private PacoteComprado pacoteComprado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @Column(nullable = false, length = 200)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desconto_id")
    private Desconto desconto;

    @Column(name = "valor_final", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorFinal;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusCobranca status;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Column(length = 200)
    private String observacoes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    private void prePersist() {
        if (this.status == null) this.status = StatusCobranca.PENDENTE;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
