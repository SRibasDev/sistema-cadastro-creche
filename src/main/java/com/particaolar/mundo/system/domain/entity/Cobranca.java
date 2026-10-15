package com.particaolar.mundo.system.domain.entity;

import com.particaolar.mundo.system.enums.StatusCobranca;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_cobrancas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cobranca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tutor_id", nullable = false)
    private Tutor tutor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospedagem_id")
    private Hospedagem hospedagem;

    @Column(nullable = false, length = 255)
    private String descricao;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal desconto = BigDecimal.ZERO;

    @Column(name = "valor_cobrado", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorCobrado;

    @Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(name = "data_vencimento", nullable = false)
    private LocalDate dataVencimento;

    @Column(name = "data_emissao", nullable = false)
    private LocalDateTime dataEmissao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusCobranca status = StatusCobranca.PENDENTE;

    @Column(length = 500)
    private String observacoes;

    @OneToMany(mappedBy = "cobranca", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pagamento> pagamentos = new ArrayList<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    private void prePersist() {
        if (this.desconto == null) {
            this.desconto = BigDecimal.ZERO;
        }
        if (this.valorPago == null) {
            this.valorPago = BigDecimal.ZERO;
        }
        if (this.valorCobrado == null) {
            this.valorCobrado = this.valorTotal.subtract(this.desconto);
        }
        if (this.dataEmissao == null) {
            this.dataEmissao = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = StatusCobranca.PENDENTE;
        }
        this.criadoEm = LocalDateTime.now();
        this.atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    private void preUpdate() {
        this.atualizadoEm = LocalDateTime.now();
    }

    public BigDecimal getSaldoDevedor() {
        return this.valorCobrado.subtract(this.valorPago);
    }
}
