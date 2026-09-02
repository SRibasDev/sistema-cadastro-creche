-- V1__create_initial_schema.sql
-- Tables: tb_usuarios, tb_tutores, tb_pets, tb_hospedagens

CREATE TABLE IF NOT EXISTS tb_usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_tutores (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    telefone VARCHAR(20) NOT NULL UNIQUE,
    cpf VARCHAR(14) UNIQUE,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_pets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    raca VARCHAR(50) NOT NULL,
    data_nascimento DATE NOT NULL,
    tutor_id BIGINT NOT NULL,
    ativo BOOLEAN DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_pets_tutor FOREIGN KEY (tutor_id) REFERENCES tb_tutores(id)
);

CREATE TABLE IF NOT EXISTS tb_hospedagens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    data_entrada DATE NOT NULL,
    data_saida DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AGENDADA',
    observacoes VARCHAR(200),
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_hospedagens_pet FOREIGN KEY (pet_id) REFERENCES tb_pets(id)
);

CREATE INDEX idx_tutores_ativo ON tb_tutores(ativo);
CREATE INDEX idx_tutores_cpf ON tb_tutores(cpf);
CREATE INDEX idx_pets_ativo ON tb_pets(ativo);
CREATE INDEX idx_pets_tutor ON tb_pets(tutor_id);
CREATE INDEX idx_hospedagens_pet ON tb_hospedagens(pet_id);
CREATE INDEX idx_hospedagens_status ON tb_hospedagens(status);