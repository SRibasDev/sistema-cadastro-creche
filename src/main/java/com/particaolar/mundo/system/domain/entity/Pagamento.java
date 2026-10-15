package com.particaolar.mundo.system.domain.entity;

import com.particaolar.mundo.system.enums.FormaPagamento;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_pagamentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cobranca_id", nullable = false)
    private Cobranca cobranca;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDateTime dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento", nullable = false, length = 30)
    private FormaPagamento formaPagamento;

    @Column(length = 100)
    private String comprovante;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "registrado_por", nullable = false, length = 100)
    private String registradoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    private void prePersist() {
        if (this.dataPagamento == null) {
            this.dataPagamento = LocalDateTime.now();
        }
        this.criadoEm = LocalDateTime.now();
    }
}
