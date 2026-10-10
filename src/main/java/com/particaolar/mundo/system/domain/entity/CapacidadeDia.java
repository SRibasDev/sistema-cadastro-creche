package com.particaolar.mundo.system.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Override de capacidade (vagas) por dia. Quando não existe linha para o dia,
 * vale a property capacidade.vagas-padrao.
 * SaaS: futuramente (tenant_id, data).
 */
@Entity
@Table(name = "tb_capacidade_dias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CapacidadeDia {

    @Id
    @Column(name = "data")
    private LocalDate data;

    @Column(nullable = false)
    private Integer vagas;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @PrePersist
    @PreUpdate
    private void prePersist() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
