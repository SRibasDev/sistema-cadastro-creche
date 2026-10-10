package com.particaolar.mundo.system.domain.entity;

import com.particaolar.mundo.system.enums.UnidadeServico;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Serviço do catálogo com preço (creche dia/meio período, hotel, banho...).
 * SaaS: futuramente (tenant_id) — hoje o catálogo é único.
 */
@Entity
@Table(name = "tb_servicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(length = 200)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UnidadeServico unidade;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    private void prePersist() {
        if (this.unidade == null) this.unidade = UnidadeServico.DIARIA;
        if (this.ativo == null) this.ativo = true;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
