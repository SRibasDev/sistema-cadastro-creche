package com.particaolar.mundo.system.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Registro de consumo de saldo de um pacote (débito feito na presença).
 */
@Entity
@Table(name = "tb_pacote_consumos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PacoteConsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pacote_comprado_id", nullable = false)
    private PacoteComprado pacoteComprado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id")
    private Reserva reserva;

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    private void prePersist() {
        if (this.quantidade == null) this.quantidade = 1;
        this.criadoEm = LocalDateTime.now();
    }
}
