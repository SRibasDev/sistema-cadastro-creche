# Status da Implementação — Sistema Cadastro Creche

> **Data de Atualização:** 15 de Outubro de 2026  
> **Branch de Trabalho:** `feature/no-mysql-h2`  
> **Commit Base:** `3aea80e`  
> **Status Geral:** Pronto para Piloto Operacional com 100% dos testes passando (85 testes, 0 falhas)

---

## 1. Resumo Executivo das Entregas

Transformamos o repositório em um sistema de gestão operacional completo, seguro e conferível, preparado para implantação piloto em uma creche/hotel de cães:

1. **Gestão de Usuários e Segurança (Fases 1 e 7.8):**
   - Adicionado ciclo de vida para contas de funcionários (`ativo`, `criadoEm`, `atualizadoEm`).
   - Sobrescrita de `UserDetails.isEnabled()` garantindo que contas desativadas tenham acesso sumariamente revogado pelo Spring Security.
   - Criados `UsuarioController` e `UsuarioService` com: listagem de equipe, atualização de dados, ativação/desativação e redefinição de senha.
   - Trava de proteção impedindo auto-desativação do administrador logado e impedindo desativação ou rebaixamento do último ADMIN do sistema.
   - Defesa em profundidade reforçada em `@DeleteMapping` e endpoints restritos.

2. **Domínio e Catálogo de Serviços (Fase 2 e 7.4):**
   - Criada a entidade `Servico` (`tb_servicos`) com precificação em `BigDecimal`, unidade de cobrança (`DIARIA`, `HORA`, `UNIDADE`), tipo de serviço (`CRECHE`, `HOTEL`, `BANHO_TOSA`, `OUTRO`) e status ativo/inativo.
   - Criados repositório, service, DTOs, mapper e controller (`/api/servicos`) com controle fino de permissões.
   - Integrado à `Hospedagem` (Reserva) com congelamento do preço acordado (`precoAcordado`) no momento da reserva, preservando o valor histórico contra alterações cadastrais posteriores.

3. **Módulo de Saúde e Manejo de Pets e Tutores (Fase 2 e 7.2, 7.3):**
   - `Pet` expandido com: `porte` (PEQUENO, MEDIO, GRANDE), `especie` (CANINO), `sexo`, `observacoes`, `alergiasRestricoes` e `vacinas`.
   - `Tutor` expandido com: `email` normalizado e `observacoes` operacionais.
   - Mapeamentos e DTOs atualizados com construtores sobrecarregados retrocompatíveis para assegurar 100% de estabilidade com a base de código e testes legados.

4. **Presença Real e Operação Diária (Fase 3 e 7.6):**
   - Check-in (`checkInEm`) e check-out (`checkOutEm`) com gravação de timestamp real da operação.
   - Registro de faltas e cancelamentos com motivo obrigatório auditável.
   - Verificação concorrente de capacidade por dia de calendário e modalidade (`CRECHE` / `HOTEL`).
   - Anti-sobreposição de reservas para o mesmo animal em intervalo semifechado `[entrada, saída)`.

5. **Módulo Financeiro e Auditoria (Fase 4 e 7.7, 6):**
   - Entidade `Cobranca` (`tb_cobrancas`) com valor total, descontos autorizados, valor cobrado líquido, valor pago acumulado e saldo devedor calculado com precisão `BigDecimal`.
   - Entidade `Pagamento` (`tb_pagamentos`) vinculada à cobrança com suporte a métodos de pagamento (`PIX`, `DINHEIRO`, `CARTAO_CREDITO`, `CARTAO_DEBITO`, `TRANSFERENCIA`), comprovante, data e operador responsável.
   - Prevenção contra pagamento maior que o saldo devedor e contra operações duplicadas via transações sincronizadas.
   - Transição automática de status da fatura: `PENDENTE` -> `PAGA_PARCIAL` -> `PAGA`.
   - Bloqueio de cancelamento de faturas com pagamentos já recebidos.
   - Entidade `AuditLog` (`tb_audit_logs`), serviço transacional autônomo `AuditLogService` e endpoint restrito `AuditLogController` (`/api/audit-logs`) para rastrear eventos operacionais (criação/cancelamento de reservas, check-in/out, faltas, emissão/baixa de cobranças, alterações de serviços e gestão de funcionários).
   - Tela dedicada de histórico de auditoria na interface web ("📜 Auditoria") com paginação para o dono (ADMIN).

6. **Dashboard e Relatórios Consolidados (Fase 5 e 7.1, 7.9):**
   - Endpoint `/api/dashboard`: métricas calculadas em tempo real (pets presentes agora, entradas previstas hoje, saídas previstas hoje, ocupação e limites de creche/hotel, faturas em aberto e montante pendente, listas de chegadas e animais presentes).
   - Endpoints `/api/relatorios/financeiro` e `/api/relatorios/reservas`: consolidação por período arbitrário de datas com validação cronológica.

7. **Interface Web Operacional Completa (Fase 5 e Seção 8):**
   - Interface em `src/main/resources/static/` moderna, escura, responsiva e sem botões quebrados ou telas falsas:
     - Visão Geral com cards dinâmicos e barras de ocupação.
     - Presença do Dia com rotina rápida de check-in/out e falta.
     - Agenda & Reservas com filtros e formulário com `datetime-local`.
     - Gestão completa de Pets e Tutores.
     - Catálogo de Serviços e Tarifas (ADMIN).
     - Gestão Financeira com emissão de faturas e baixa de pagamentos com recibo/comprovante.
     - Gestão de Funcionários com redefinição de senhas e ativação de contas.
     - Relatórios operacionais e de faturamento.
     - Adaptação visual dos controles de acordo com o perfil (`ADMIN` vs `FUNCIONARIO`).

8. **Migrações Flyway Versionadas:**
   - `V1__create_initial_schema.sql` (schema inicial)
   - `V2__normalize_personal_data.sql` (normalização de CPF/telefone/email)
   - `V3__reservas_data_hora_modalidade.sql` (data/hora e capacidade)
   - `V4__reserva_motivo_cancelamento_falta.sql` (motivo de falta e cancelamento)
   - `V5__servicos_e_campos_pet_tutor.sql` (catálogo de serviços e expansão de pet/tutor/hospedagem)
   - `V6__financeiro_cobrancas_e_pagamentos.sql` (faturas e pagamentos)
   - `V7__usuarios_status_e_audit_logs.sql` (status de usuário e log de auditoria)

---

## 2. Resultados dos Testes Automatizados

Execução real realizada via Maven Surefire:
- **Testes executados:** 91
- **Falhas:** 0
- **Erros:** 0
- **Ignorados:** 0
- **Status do Build:** `BUILD SUCCESS`

### Detalhamento por Classe de Teste:
- `AuthSecurityRegressionTest`: 7 testes (proteção de registro anônimo, privilégios de funcionário, swagger restrito, bloqueio de usuário desativado, listagem restrita de equipe).
- `SecurityIntegrationTest`: 9 testes (rejeição sem token, token inválido, token expirado, proibição de deleção para funcionário, tutor inativo 404, validações de DTO, credenciais inválidas 401).
- `AuditLogServiceTest`: 6 testes (registro com usuário explícito, fallback ao SecurityContextHolder, fallback anônimo para SISTEMA, resiliência a falhas de persistência, paginação e validação de limite de página).
- `UsuarioServiceTest`: 13 testes (listagem paginada, busca por id, atualização cadastral, bloqueio de e-mail duplicado, proteção do único admin ativo, desativação de equipe, proteção contra auto-desativação, alteração de senha com validação da atual, redefinição por admin sem senha atual, validação de `isEnabled()`).
- `HospedagemServiceTest`: 13 testes (reserva válida, intervalo inválido, sobreposição por pet, reservas adjacentes sem conflito, capacidade máxima por modalidade/dia, reagendamento com capacidade reavaliada, transições de status válidas e inválidas, check-in, check-out, falta com motivo, cancelamento com motivo).
- `ServicoServiceTest`: 7 testes (salvar serviço, unicidade de nome, listagem de ativos, busca por id, busca por tipo, atualização cadastral, ativação/desativação).
- `CobrancaServiceTest`: 8 testes (emissão de cobrança com desconto, impedimento de desconto abusivo, baixa parcial, baixa integral com transição para PAGA, bloqueio de pagamento excedente, bloqueio em cobrança quitada, cancelamento sem pagamentos, bloqueio de cancelamento com pagamentos).
- `DashboardServiceTest`: 1 teste (consolidação de métricas operacionais, capacidade e ocupação).
- `PetServiceTest`: 8 testes (CRUD de pet, vínculo obrigatório com tutor, exclusão lógica).
- `TutorServiceTest`: 13 testes (CRUD de tutor, unicidade de CPF e telefone normalizados, exclusão lógica).
- `NormalizadorDadosTest`: 5 testes (normalização canônica de CPF, telefone e email).
- `SystemApplicationTests`: 1 teste (inicialização do contexto do Spring Boot).

---

## 3. Como Executar Localmente

### Pré-requisitos:
- Java 21
- Maven (`./mvnw.cmd` incluído)

### Execução em modo Desenvolvimento (H2 em arquivo local, sem MySQL/Docker):
```bash
./mvnw.cmd spring-boot:run
```
- Acesse a interface web: `http://localhost:8080/`
- Banco H2 persistido em: `./data/creche-db.mv.db`

### Executar a Suite de Testes:
```bash
./mvnw.cmd test
```

### Provisionamento Seguro do 1º Administrador (Dono):
Defina no ambiente ou `.env`:
```env
APP_BOOTSTRAP_ADMIN_ENABLED=true
APP_BOOTSTRAP_ADMIN_EMAIL=admin@creche.com
APP_BOOTSTRAP_ADMIN_PASSWORD=SenhaForteAdmin@2026
APP_BOOTSTRAP_ADMIN_NOME="Dono da Creche"
```
Ao iniciar, o primeiro administrador será provisionado e a senha nunca será registrada em log.
Após a inicialização, desligue a flag (`APP_BOOTSTRAP_ADMIN_ENABLED=false`).
