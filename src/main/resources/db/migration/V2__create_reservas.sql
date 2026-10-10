-- V2__create_reservas.sql
-- Evolui Hospedagem -> Reserva: tipo (CRECHE/HOTEL), horários, motivo de
-- cancelamento, histórico de reagendamento e capacidade por dia.
-- tb_hospedagens é MANTIDA (dados copiados para tb_reservas; down script abaixo).

CREATE TABLE IF NOT EXISTS tb_reservas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL DEFAULT 'HOTEL',
    data_entrada DATE NOT NULL,
    data_saida DATE NOT NULL,
    hora_entrada TIME NULL,
    hora_saida TIME NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADA',
    observacoes VARCHAR(200),
    motivo_cancelamento VARCHAR(200),
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_reservas_pet FOREIGN KEY (pet_id) REFERENCES tb_pets(id)
);

CREATE TABLE IF NOT EXISTS tb_reserva_historicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reserva_id BIGINT NOT NULL,
    tipo_evento VARCHAR(30) NOT NULL,        -- CRIACAO, REAGENDAMENTO, CANCELAMENTO, STATUS_ALTERADO
    data_anterior DATE NULL,
    data_saida_anterior DATE NULL,
    data_nova DATE NULL,
    data_saida_nova DATE NULL,
    status_anterior VARCHAR(20) NULL,
    status_novo VARCHAR(20) NULL,
    motivo VARCHAR(200) NULL,
    usuario VARCHAR(100) NULL,
    criado_em DATETIME NOT NULL,
    CONSTRAINT fk_historico_reserva FOREIGN KEY (reserva_id) REFERENCES tb_reservas(id)
);

-- Capacidade por dia (override do padrão em capacidade.vagas-padrao).
-- SaaS: futuramente (tenant_id, data).
CREATE TABLE IF NOT EXISTS tb_capacidade_dias (
    data DATE PRIMARY KEY,
    vagas INT NOT NULL,
    atualizado_em DATETIME NOT NULL
);

-- Migra dados existentes: hospedagens viram reservas de HOTEL (reversível).
INSERT INTO tb_reservas (pet_id, tipo, data_entrada, data_saida, hora_entrada, hora_saida,
                         status, observacoes, motivo_cancelamento, criado_em, atualizado_em)
SELECT h.pet_id, 'HOTEL', h.data_entrada, h.data_saida, NULL, NULL,
       h.status, h.observacoes, NULL, h.criado_em, h.atualizado_em
FROM tb_hospedagens h
WHERE NOT EXISTS (SELECT 1 FROM tb_reservas r WHERE r.pet_id = h.pet_id
                    AND r.data_entrada = h.data_entrada AND r.data_saida = h.data_saida);

CREATE INDEX idx_reservas_pet ON tb_reservas(pet_id);
CREATE INDEX idx_reservas_status ON tb_reservas(status);
CREATE INDEX idx_reservas_periodo ON tb_reservas(data_entrada, data_saida);
CREATE INDEX idx_reservas_tipo ON tb_reservas(tipo);
CREATE INDEX idx_historico_reserva ON tb_reserva_historicos(reserva_id);

-- ---------------------------------------------------------------------------
-- DOWN (reversível): descomentar para reverter.
-- DROP TABLE tb_reserva_historicos;
-- DROP TABLE tb_capacidade_dias;
-- DROP TABLE tb_reservas;
-- (tb_hospedagens permanece intacta)
-- ---------------------------------------------------------------------------
