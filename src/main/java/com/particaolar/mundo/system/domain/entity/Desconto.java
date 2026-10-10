package com.particaolar.mundo.system.domain.entity;

import com.particaolar.mundo.system.enums.TipoDesconto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Desconto/cupom promocional (ex.: 10% na 10ª diária).
 */
@Entity
@Table(name = "tb_descontos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Desconto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDesconto tipo;

    /** Percentual (0-100) ou valor em R$, conforme o tipo. */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    /** NULL = sem validade. */
    @Column
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
