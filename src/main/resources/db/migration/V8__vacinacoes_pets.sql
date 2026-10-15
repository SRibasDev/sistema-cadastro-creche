-- V8__vacinacoes_pets.sql
-- Registro de vacinações dos pets com controle de vencimento

CREATE TABLE IF NOT EXISTS tb_vacinacoes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    nome_vacina VARCHAR(100) NOT NULL,
    data_aplicacao DATE NOT NULL,
    data_vencimento DATE NOT NULL,
    lote VARCHAR(50) NULL,
    veterinario VARCHAR(100) NULL,
    observacoes VARCHAR(500) NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_vacinacoes_pet FOREIGN KEY (pet_id) REFERENCES tb_pets(id),
    CONSTRAINT chk_vacinacao_vencimento CHECK (data_vencimento >= data_aplicacao)
);

CREATE INDEX idx_vacinacoes_pet ON tb_vacinacoes(pet_id);
CREATE INDEX idx_vacinacoes_vencimento ON tb_vacinacoes(data_vencimento);
CREATE INDEX idx_vacinacoes_ativo ON tb_vacinacoes(ativo);
