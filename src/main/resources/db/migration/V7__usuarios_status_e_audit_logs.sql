-- V7__usuarios_status_e_audit_logs.sql
-- Controle de status ativo/desativo em usuários e tabela de auditoria operacional

ALTER TABLE tb_usuarios
    ADD COLUMN ativo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN criado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN atualizado_em DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX idx_usuarios_ativo ON tb_usuarios(ativo);

CREATE TABLE IF NOT EXISTS tb_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(100) NOT NULL,
    acao VARCHAR(50) NOT NULL,
    entidade VARCHAR(50) NOT NULL,
    entidade_id BIGINT NULL,
    detalhes VARCHAR(1000) NULL,
    data_hora DATETIME NOT NULL
);

CREATE INDEX idx_audit_logs_data ON tb_audit_logs(data_hora);
CREATE INDEX idx_audit_logs_entidade ON tb_audit_logs(entidade, entidade_id);
