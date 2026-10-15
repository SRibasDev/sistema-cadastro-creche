package com.particaolar.mundo.system.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro de vacinação de um pet: qual vacina foi aplicada, quando,
 * quando vence, lote e responsável pela aplicação.
 */
@Entity
@Table(name = "tb_vacinacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Vacinacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @Column(nullable = false, length = 100)
    private String nomeVacina;

    @Column(nullable = false)
    private LocalDate dataAplicacao;

    @Column(nullable = false)
    private LocalDate dataVencimento;

    @Column(length = 50)
    private String lote;

    @Column(length = 100)
    private String veterinario;

    @Column(length = 500)
    private String observacoes;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    private void prePersist() {
        if (this.ativo == null) {
            this.ativo = true;
        }
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    /** Situação calculada a partir da data de vencimento. */
    @Transient
    public String getSituacao() {
        if (dataVencimento == null) {
            return "SEM_VENCIMENTO";
        }
        LocalDate hoje = LocalDate.now();
        if (dataVencimento.isBefore(hoje)) {
            return "VENCIDA";
        }
        if (!dataVencimento.isAfter(hoje.plusDays(30))) {
            return "A_VENCER";
        }
        return "EM_DIA";
    }
}
