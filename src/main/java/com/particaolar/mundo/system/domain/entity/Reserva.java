package com.particaolar.mundo.system.domain.entity;

import com.particaolar.mundo.system.enums.StatusReserva;
import com.particaolar.mundo.system.enums.TipoReserva;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Reserva de CRECHE ou HOTEL (evolução da antiga Hospedagem;
 * a tabela antiga tb_hospedagens permanece como histórico).
 */
@Entity
@Table(name = "tb_reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoReserva tipo;

    @Column(name = "data_entrada", nullable = false)
    private LocalDate dataEntrada;

    @Column(name = "data_saida", nullable = false)
    private LocalDate dataSaida;

    @Column(name = "hora_entrada")
    private LocalTime horaEntrada;

    @Column(name = "hora_saida")
    private LocalTime horaSaida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusReserva status;

    @Column(length = 200)
    private String observacoes;

    @Column(name = "motivo_cancelamento", length = 200)
    private String motivoCancelamento;

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
        if (this.status == null) {
            this.status = StatusReserva.AGENDADA;
        }
        if (this.tipo == null) {
            this.tipo = TipoReserva.HOTEL;
        }
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    /** Período coberto por esta reserva em um determinado dia. */
    public boolean cobreDia(LocalDate dia) {
        return !dia.isBefore(dataEntrada) && !dia.isAfter(dataSaida);
    }
}
