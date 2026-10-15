-- V5__servicos_e_campos_pet_tutor.sql
-- Catálogo de serviços, preços e expansão dos cadastros de pets e tutores

CREATE TABLE IF NOT EXISTS tb_servicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    tipo VARCHAR(30) NOT NULL,
    preco DECIMAL(10,2) NOT NULL,
    unidade VARCHAR(20) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL
);

CREATE INDEX idx_servicos_ativo ON tb_servicos(ativo);
CREATE INDEX idx_servicos_tipo ON tb_servicos(tipo);

-- Novos atributos para saúde e manejo de pets
ALTER TABLE tb_pets
    ADD COLUMN porte VARCHAR(20) NULL,
    ADD COLUMN especie VARCHAR(50) NOT NULL DEFAULT 'CANINO',
    ADD COLUMN sexo VARCHAR(20) NULL,
    ADD COLUMN observacoes VARCHAR(500) NULL,
    ADD COLUMN alergias_restricoes VARCHAR(500) NULL,
    ADD COLUMN vacinas VARCHAR(500) NULL;

-- Novos atributos para tutores
ALTER TABLE tb_tutores
    ADD COLUMN email VARCHAR(100) NULL,
    ADD COLUMN observacoes VARCHAR(500) NULL;

-- Associação de serviço, preço histórico e data/hora real de presença em reservas
ALTER TABLE tb_hospedagens
    ADD COLUMN servico_id BIGINT NULL,
    ADD COLUMN preco_acordado DECIMAL(10,2) NULL,
    ADD COLUMN check_in_em DATETIME NULL,
    ADD COLUMN check_out_em DATETIME NULL;

ALTER TABLE tb_hospedagens
    ADD CONSTRAINT fk_hospedagens_servico FOREIGN KEY (servico_id) REFERENCES tb_servicos(id);
