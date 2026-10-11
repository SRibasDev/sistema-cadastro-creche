-- V3__reservas_data_hora_modalidade.sql
-- Evolui reservas para data+hora e modalidade, com índices para as
-- verificações de sobreposição (por pet) e capacidade (por dia/modalidade).
-- Idempotente apenas em base nova; em base V1 existente, converte DATE->DATETIME
-- (meia-noite) e preenche modalidade='HOTEL' via DEFAULT.

ALTER TABLE tb_hospedagens
    MODIFY COLUMN data_entrada DATETIME(6) NOT NULL,
    MODIFY COLUMN data_saida DATETIME(6) NOT NULL;

ALTER TABLE tb_hospedagens
    ADD COLUMN modalidade VARCHAR(20) NOT NULL DEFAULT 'HOTEL';

ALTER TABLE tb_hospedagens
    ADD CONSTRAINT chk_hospedagens_intervalo CHECK (data_saida > data_entrada);

CREATE INDEX idx_hospedagens_modalidade_status ON tb_hospedagens(modalidade, status);
CREATE INDEX idx_hospedagens_periodo ON tb_hospedagens(data_entrada, data_saida);
