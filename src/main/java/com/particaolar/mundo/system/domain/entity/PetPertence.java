package com.particaolar.mundo.system.domain.entity;

import com.particaolar.mundo.system.enums.TipoPertence;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Pertence do cão (coleira, ração, remédios, brinquedos...).
 * SaaS: futuramente (tenant_id, pet_id) — o pet já é escopo do tenant.
 */
@Entity
@Table(name = "tb_pertences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PetPertence {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoPertence tipo;

    @Column(nullable = false, length = 150)
    private String descricao;

    @Column(length = 50)
    private String quantidade;

    @Column(name = "quantidade_numero")
    private Integer quantidadeNumero;

    @Column(length = 200)
    private String observacoes;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    @PrePersist
    private void prePersist() {
        if (this.ativo == null) {
            this.ativo = true;
        }
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }
}
