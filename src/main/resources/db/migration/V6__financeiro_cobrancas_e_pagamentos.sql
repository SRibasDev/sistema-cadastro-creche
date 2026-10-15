-- V6__financeiro_cobrancas_e_pagamentos.sql
-- Módulo financeiro: faturas (cobranças) e registros de pagamentos

CREATE TABLE IF NOT EXISTS tb_cobrancas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tutor_id BIGINT NOT NULL,
    hospedagem_id BIGINT NULL,
    descricao VARCHAR(255) NOT NULL,
    valor_total DECIMAL(10,2) NOT NULL,
    desconto DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    valor_cobrado DECIMAL(10,2) NOT NULL,
    valor_pago DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    data_vencimento DATE NOT NULL,
    data_emissao DATETIME NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDENTE',
    observacoes VARCHAR(500) NULL,
    criado_em DATETIME NOT NULL,
    atualizado_em DATETIME NOT NULL,
    CONSTRAINT fk_cobrancas_tutor FOREIGN KEY (tutor_id) REFERENCES tb_tutores(id),
    CONSTRAINT fk_cobrancas_hospedagem FOREIGN KEY (hospedagem_id) REFERENCES tb_hospedagens(id)
);

CREATE INDEX idx_cobrancas_tutor ON tb_cobrancas(tutor_id);
CREATE INDEX idx_cobrancas_status ON tb_cobrancas(status);
CREATE INDEX idx_cobrancas_vencimento ON tb_cobrancas(data_vencimento);

CREATE TABLE IF NOT EXISTS tb_pagamentos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cobranca_id BIGINT NOT NULL,
    valor DECIMAL(10,2) NOT NULL,
    data_pagamento DATETIME NOT NULL,
    forma_pagamento VARCHAR(30) NOT NULL,
    comprovante VARCHAR(100) NULL,
    observacoes VARCHAR(500) NULL,
    registrado_por VARCHAR(100) NOT NULL,
    criado_em DATETIME NOT NULL,
    CONSTRAINT fk_pagamentos_cobranca FOREIGN KEY (cobranca_id) REFERENCES tb_cobrancas(id)
);

CREATE INDEX idx_pagamentos_cobranca ON tb_pagamentos(cobranca_id);
CREATE INDEX idx_pagamentos_data ON tb_pagamentos(data_pagamento);
