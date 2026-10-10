package com.particaolar.mundo.system.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Pacote comprado por um pet: carrega o SALDO que será debitado
 * automaticamente quando a presença for marcada (PresencaService).
 * SaaS: futuramente (tenant_id, pet_id) — pet já é escopo do tenant.
 */
@Entity
@Table(name = "tb_pacotes_comprados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacoteComprado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pacote_id", nullable = false)
    private Pacote pacote;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false)
    private Integer saldo;

    @Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago;

    @Column(nullable = false)
    private LocalDate validade;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    private void prePersist() {
        if (this.ativo == null) this.ativo = true;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
