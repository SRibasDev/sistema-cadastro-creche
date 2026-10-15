package com.particaolar.mundo.system.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String usuario;

    @Column(nullable = false, length = 50)
    private String acao;

    @Column(nullable = false, length = 50)
    private String entidade;

    @Column(name = "entidade_id")
    private Long entidadeId;

    @Column(length = 1000)
    private String detalhes;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @PrePersist
    private void prePersist() {
        if (this.dataHora == null) {
            this.dataHora = LocalDateTime.now();
        }
    }
}
