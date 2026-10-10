-- V4__create_servicos_pacotes.sql
-- Catálogo de serviços com preço, descontos/cupons e pacotes/combos
-- promocionais com saldo e validade (compra por pet).
-- Valores monetários: DECIMAL(10,2) + BigDecimal na aplicação.
-- Reversível: ver DOWN no final.

CREATE TABLE IF NOT EXISTS tb_servicos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(80) NOT NULL,
    descricao VARCHAR(200),
    preco DECIMAL(10,2) NOT NULL,
    unidade VARCHAR(20) NOT NULL DEFAULT 'DIARIA',  -- DIARIA, MEIO_PERIODO, AVULSO, MENSAL
    ativo BOOLEAN DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_descontos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    codigo VARCHAR(30) NOT NULL UNIQUE,             -- cupom
    nome VARCHAR(80) NOT NULL,
    tipo VARCHAR(20) NOT NULL,                      -- PERCENTUAL, VALOR
    valor DECIMAL(10,2) NOT NULL,                   -- % (0-100) ou R$
    validade DATE,                                  -- NULL = sem validade
    ativo BOOLEAN DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL
);

-- Pacote/Combo promocional (ex.: 10 diárias, creche+banho, plano mensal).
CREATE TABLE IF NOT EXISTS tb_pacotes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(80) NOT NULL,
    descricao VARCHAR(200),
    servico_id BIGINT NOT NULL,                     -- serviço base (diária creche, hotel...)
    quantidade INT NOT NULL,                        -- 10 diárias, 1 mês...
    preco_total DECIMAL(10,2) NOT NULL,
    validade_dias INT NOT NULL DEFAULT 90,          -- validade do saldo após a compra
    desconto_id BIGINT,                             -- cupom já aplicado na composição (opcional)
    ativo BOOLEAN DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_pacote_servico FOREIGN KEY (servico_id) REFERENCES tb_servicos(id),
    CONSTRAINT fk_pacote_desconto FOREIGN KEY (desconto_id) REFERENCES tb_descontos(id)
);

-- Saldo comprado por um pet (pacote ativo).
CREATE TABLE IF NOT EXISTS tb_pacotes_comprados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pacote_id BIGINT NOT NULL,
    pet_id BIGINT NOT NULL,
    saldo INT NOT NULL,                             -- diárias/sessões restantes
    valor_pago DECIMAL(10,2) NOT NULL,
    validade DATE NOT NULL,                         -- data limite de uso
    ativo BOOLEAN DEFAULT TRUE,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_pacote_comprado_pacote FOREIGN KEY (pacote_id) REFERENCES tb_pacotes(id),
    CONSTRAINT fk_pacote_comprado_pet FOREIGN KEY (pet_id) REFERENCES tb_pets(id)
);

-- Trilha de consumo de saldo (debito na presença).
CREATE TABLE IF NOT EXISTS tb_pacote_consumos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    pacote_comprado_id BIGINT NOT NULL,
    reserva_id BIGINT,                              -- origem do consumo (presença)
    quantidade INT NOT NULL DEFAULT 1,
    criado_em DATETIME NOT NULL,
    CONSTRAINT fk_consumo_pacote FOREIGN KEY (pacote_comprado_id) REFERENCES tb_pacotes_comprados(id),
    CONSTRAINT fk_consumo_reserva FOREIGN KEY (reserva_id) REFERENCES tb_reservas(id)
);

CREATE INDEX idx_servico_ativo ON tb_servicos(ativo);
CREATE INDEX idx_pacote_ativo ON tb_pacotes(ativo);
CREATE INDEX idx_pacote_comprado_pet ON tb_pacotes_comprados(pet_id);
CREATE INDEX idx_pacote_comprado_ativo ON tb_pacotes_comprados(ativo);
CREATE INDEX idx_consumo_pacote ON tb_pacote_consumos(pacote_comprado_id);

-- ---------------------------------------------------------------------------
-- DOWN (reversível): descomentar para reverter.
-- DROP TABLE tb_pacote_consumos;
-- DROP TABLE tb_pacotes_comprados;
-- DROP TABLE tb_pacotes;
-- DROP TABLE tb_descontos;
-- DROP TABLE tb_servicos;
-- ---------------------------------------------------------------------------
