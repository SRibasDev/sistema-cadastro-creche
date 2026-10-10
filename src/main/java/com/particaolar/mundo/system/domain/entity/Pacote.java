package com.particaolar.mundo.system.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Pacote/Combo promocional do catálogo (ex.: 10 diárias, creche + banho,
 * plano mensal). A compra gera um PacoteComprado com saldo por pet.
 */
@Entity
@Table(name = "tb_pacotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pacote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(length = 200)
    private String descricao;

    /** Serviço base consumido pelo pacote (ex.: diária de creche). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    /** Quantidade de unidades incluídas (10 diárias, 1 mês...). */
    @Column(nullable = false)
    private Integer quantidade;

    @Column(name = "preco_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoTotal;

    /** Validade do saldo (em dias) a partir da compra. */
    @Column(name = "validade_dias", nullable = false)
    private Integer validadeDias = 90;

    /** Cupom aplicado na composição do pacote (opcional). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desconto_id")
    private Desconto desconto;

    @Column(nullable = false)
    private Boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    private void prePersist() {
        if (this.ativo == null) this.ativo = true;
        if (this.validadeDias == null) this.validadeDias = 90;
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
