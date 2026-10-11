-- V4__reserva_motivo_cancelamento_falta.sql
-- Adiciona motivo de cancelamento/falta e permite o status FALTA.
-- CHECK do intervalo usa nome diferente do V3 para evitar colisão.

ALTER TABLE tb_hospedagens
    ADD COLUMN motivo_cancelamento VARCHAR(500) NULL;

ALTER TABLE tb_hospedagens
    ADD CONSTRAINT chk_hospedagens_cancelamento_motivo
    CHECK (
        (status NOT IN ('CANCELADA', 'FALTA'))
        OR (motivo_cancelamento IS NOT NULL AND CHAR_LENGTH(motivo_cancelamento) >= 3)
    );
