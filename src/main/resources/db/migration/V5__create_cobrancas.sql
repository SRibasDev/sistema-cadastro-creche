-- V5__create_cobrancas.sql
-- Cobranças geradas por reserva ou pacote, com vencimento, status e
-- pagamento manual (sem integração de gateway nesta fase).
-- Reversível: ver DOWN no final.

CREATE TABLE IF NOT EXISTS tb_cobrancas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    origem VARCHAR(20) NOT NULL,                    -- RESERVA, PACOTE
    reserva_id BIGINT,
    pacote_comprado_id BIGINT,
    pet_id BIGINT NOT NULL,
    tutor_id BIGINT NOT NULL,
    descricao VARCHAR(200) NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    desconto_id BIGINT,                             -- desconto aplicado (opcional)
    valor_final DECIMAL(10,2) NOT NULL,             -- valor após desconto
    data_vencimento DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE', -- PENDENTE, PAGA, ATRASADA, CANCELADA
    data_pagamento DATE,
    observacoes VARCHAR(200),
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_cobranca_reserva FOREIGN KEY (reserva_id) REFERENCES tb_reservas(id),
    CONSTRAINT fk_cobranca_pacote FOREIGN KEY (pacote_comprado_id) REFERENCES tb_pacotes_comprados(id),
    CONSTRAINT fk_cobranca_pet FOREIGN KEY (pet_id) REFERENCES tb_pets(id),
    CONSTRAINT fk_cobranca_tutor FOREIGN KEY (tutor_id) REFERENCES tb_tutores(id),
    CONSTRAINT fk_cobranca_desconto FOREIGN KEY (desconto_id) REFERENCES tb_descontos(id)
);

CREATE INDEX idx_cobranca_status ON tb_cobrancas(status);
CREATE INDEX idx_cobranca_vencimento ON tb_cobrancas(data_vencimento);
CREATE INDEX idx_cobranca_tutor ON tb_cobrancas(tutor_id);
CREATE INDEX idx_cobranca_pet ON tb_cobrancas(pet_id);
CREATE INDEX idx_cobranca_status_venc ON tb_cobrancas(status, data_vencimento);

-- ---------------------------------------------------------------------------
-- DOWN (reversível): descomentar para reverter.
-- DROP TABLE tb_cobrancas;
-- ---------------------------------------------------------------------------
