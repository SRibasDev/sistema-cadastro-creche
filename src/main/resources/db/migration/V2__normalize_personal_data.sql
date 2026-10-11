-- V2__normalize_personal_data.sql
-- Normaliza dados pessoais já persistidos para a forma canônica (apenas dígitos),
-- garantindo unicidade correta de CPF e telefone. Idempotente.

-- CPF: remove pontos e traços.
UPDATE tb_tutores
SET cpf = REPLACE(REPLACE(REPLACE(cpf, '.', ''), '-', ''), ' ', '')
WHERE cpf IS NOT NULL;

-- Telefone: remove parênteses, espaços e traços.
UPDATE tb_tutores
SET telefone = REPLACE(REPLACE(REPLACE(REPLACE(telefone, '(', ''), ')', ''), '-', ''), ' ', '')
WHERE telefone IS NOT NULL;

-- Usuários: e-mail em minúsculas (forma canônica usada no login).
UPDATE tb_usuarios
SET email = LOWER(TRIM(email))
WHERE email IS NOT NULL;
