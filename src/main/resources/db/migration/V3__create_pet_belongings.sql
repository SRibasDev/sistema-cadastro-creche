-- V3__create_pet_belongings.sql
-- Pertences do cão (coleira, ração, remédios, etc.) e alertas/observações
-- no próprio pet (alergias, comportamento...).
-- Reversível: ver script DOWN no final.

CREATE TABLE IF NOT EXISTS tb_pertences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    tipo VARCHAR(30) NOT NULL,            -- COLEIRA, RACAO, REMEDIO, BRINQUEDO, DOCUMENTO, OUTRO
    descricao VARCHAR(150) NOT NULL,
    quantidade VARCHAR(50),               -- ex.: "1 saco 15kg", "2 comprimidos/dia"
    quantidade_numero INT,                -- quantidade estruturada (opcional)
    observacoes VARCHAR(200),
    ativo BOOLEAN DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_pertence_pet FOREIGN KEY (pet_id) REFERENCES tb_pets(id)
);

-- Alertas e observações gerais direto no pet (LGPD: dados do pet).
ALTER TABLE tb_pets ADD COLUMN alertas VARCHAR(500);
ALTER TABLE tb_pets ADD COLUMN observacoes_gerais VARCHAR(500);

CREATE INDEX idx_pertence_pet ON tb_pertences(pet_id);
CREATE INDEX idx_pertence_ativo ON tb_pertences(ativo);
CREATE INDEX idx_pertence_tipo ON tb_pertences(tipo);

-- ---------------------------------------------------------------------------
-- DOWN (reversível): descomentar para reverter.
-- DROP TABLE tb_pertences;
-- ALTER TABLE tb_pets DROP COLUMN alertas;
-- ALTER TABLE tb_pets DROP COLUMN observacoes_gerais;
-- ---------------------------------------------------------------------------
