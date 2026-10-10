package com.particaolar.mundo.system.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Trilha de auditoria de uma Reserva: criação, reagendamento, cancelamento
 * e mudanças de status. SaaS: futuramente (tenant_id, reserva_id já existe).
 */
@Entity
@Table(name = "tb_reserva_historicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservaHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reserva_id", nullable = false)
    private Reserva reserva;

    @Column(name = "tipo_evento", nullable = false, length = 30)
    private String tipoEvento; // CRIACAO, REAGENDAMENTO, CANCELAMENTO, STATUS_ALTERADO

    @Column(name = "data_anterior")
    private LocalDate dataAnterior;

    @Column(name = "data_saida_anterior")
    private LocalDate dataSaidaAnterior;

    @Column(name = "data_nova")
    private LocalDate dataNova;

    @Column(name = "data_saida_nova")
    private LocalDate dataSaidaNova;

    @Column(name = "status_anterior", length = 20)
    private String statusAnterior;

    @Column(name = "status_novo", length = 20)
    private String statusNovo;

    @Column(length = 200)
    private String motivo;

    @Column(length = 100)
    private String usuario;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    private void prePersist() {
        this.criadoEm = LocalDateTime.now();
    }
}
