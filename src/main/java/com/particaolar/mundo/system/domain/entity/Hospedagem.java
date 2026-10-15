package com.particaolar.mundo.system.domain.entity;

import com.particaolar.mundo.system.enums.Modalidade;
import com.particaolar.mundo.system.enums.StatusHospedagem;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Representa uma reserva (creche ou hotel) para um pet.
 *
 * <p>O intervalo é semifechado [dataEntrada, dataSaida): a saída de uma reserva
 * pode coincidir com a entrada da seguinte. A capacidade é avaliada por data de
 * calendário (de dataEntrada a dataSaida, inclusive).</p>
 */
@Entity
@Table(name = "tb_hospedagens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hospedagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    /** Modalidade da reserva (CRECHE ou HOTEL). */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidade modalidade;

    /** Entrada com data e horário. */
    @Column(name = "data_entrada", nullable = false)
    private LocalDateTime dataEntrada;

    /** Saída com data e horário. */
    @Column(name = "data_saida", nullable = false)
    private LocalDateTime dataSaida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusHospedagem status;

    @Column(length = 200)
    private String observacoes;

    /** Motivo do cancelamento ou da falta (obrigatório nessas transições). */
    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    private Servico servico;

    @Column(name = "preco_acordado", precision = 10, scale = 2)
    private java.math.BigDecimal precoAcordado;

    @Column(name = "check_in_em")
    private LocalDateTime checkInEm;

    @Column(name = "check_out_em")
    private LocalDateTime checkOutEm;

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
            this.status = StatusHospedagem.AGENDADA;
        }
        if (this.modalidade == null) {
            this.modalidade = Modalidade.HOTEL;
        }
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }
}