# Diagnóstico e Plano — Sistema de Gestão Creche/Hotel de Cães

> Auditoria executada em 10/10/2026 sobre o commit `a1fa6c2`.
> Stack confirmada pela leitura do código real (não presumida).

---

## FASE 1 — AUDITORIA

### Stack confirmada
| Camada | Tecnologia (confirmada no código) |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.4.1 |
| Segurança | Spring Security + JWT (JJWT 0.12.3), BCrypt(12) |
| Persistência | Spring Data JPA + Hibernate |
| Banco | MySQL 8 (produção) / H2 (testes) |
| Migrações | Flyway (V1 inicial) |
| Build | Maven (`mvnw`), Surefire, JaCoCo |
| API docs | springdoc-openapi (Swagger) |
| Observabilidade | Actuator + Micrometer Prometheus |
| Frontend | **Apenas UI estática** HTML/CSS/JS de teste em `static/` (NÃO é React) |
| Testes | JUnit 5 + Mockito + MockMvc |

### Estrutura (monólito em camadas)
```
config/     SecurityConfig, DataSeeder
controller/ TutorController, PetController, HospedagemController, security/AuthController
domain/     entity/ repository/ service/
dto/        request/ response/
enums/      Role, Porte, StatusHospedagem
exception/  handler/ (GlobalExceptionHandler, ErrorResponse)
mapper/     TutorMapper, PetMapper, HospedagemMapper
security/   controller/ dto/ entity/Usuario filter/JwtAuthFilter repository/ service/
resources/  db/migration/V1, static/ (UI de teste)
```

### Funcionalidades existentes (funcionando, validadas por testes)
- CRUD Tutores (paginação, validação CPF/telefone, exclusão lógica)
- CRUD Pets (associado a tutor, exclusão lógica)
- Hospedagens (status + máquina de transições, cancelamento)
- Login JWT, dois perfis (ADMIN, FUNCIONARIO)
- Tratamento global de erros, respostas padronizadas
- Exclusão lógica (`ativo`) e timestamps de auditoria básicos

### Baseline de testes (executado de verdade)
```
SecurityIntegrationTest ....... 9 tests, 0 failures
HospedagemServiceTest ........ 19 tests, 0 failures
PetServiceTest ...............  8 tests, 0 failures
TutorServiceTest ............. 11 tests, 0 failures
SystemApplicationTests .......  1 test,  0 failures
TOTAL ........................ 48 tests, 0 failures  ✅ BUILD SUCCESS
```


### FASE 4 — FLUXOS OPERACIONAIS: RESERVAS, CAPACIDADE E PRESENÇA (executada em 10/10/2026)

### Decisões de negócio consolidadas
- **Reserva ≠ presença.** Reserva é `AGENDADA`; presença é `check-in` → `EM_ANDAMENTO` → `check-out` → `CONCLUIDA`.
- **Intervalo semifechado `[entrada, saída)`:** reservas adjacentes (saída == próxima entrada) NÃO conflitam.
- **Sobreposição por pet:** qualquer interseção com reserva ativa (`AGENDADA`, `EM_ANDAMENTO`) do mesmo pet → `409 ReservaConflitanteException`.
- **Capacidade por dia de calendário e modalidade:** cada dia de `[entrada, saída]` (inclusive) conta ocupação; `ocupadas >= capacidade` → `409 CapacidadeExcedidaException`. Padrões: creche=10, hotel=20 (env `APP_CRECHE_CAPACIDADE_*`).
- **Concorrência (1 instância):** `synchronized(reservaLock)` + `TransactionTemplate` (commit DENTRO do lock). Para multi-instância, evoluir para lock pessimista no banco.
- **Presença:** `check-in` exige `AGENDADA`; `check-out` exige `EM_ANDAMENTO`; duplicados/inversos → `400`. `falta` e `cancelar` exigem motivo ≥3 chars, gravado em `motivo_cancelamento`.
- **Permissões:** leitura para `ADMIN`+`FUNCIONARIO`; criação/edição/cancelamento de reservas, tutores e pets só `ADMIN`; `check-in/out/falta` liberados para `FUNCIONARIO`.

### Arquivos alterados/criados nesta fase
- `enums/Modalidade.java` (novo: CRECHE, HOTEL)
- `enums/StatusHospedagem.java` (+FALTA)
- `domain/entity/Hospedagem.java` (modalidade, LocalDateTime, motivo_cancelamento)
- `domain/repository/HospedagemRepository.java` (queries sobreposição + contagem/dia)
- `domain/service/HospedagemService.java` (salvar/atualizar atômicos, presença, helpers)
- `config/TransactionConfig.java` (novo: bean TransactionTemplate)
- `controller/HospedagemController.java` (+check-in/out/falta, @PreAuthorize finos)
- `controller/TutorController.java`, `controller/PetController.java` (matriz de permissões)
- `dto/request/HospedagemRequestDTO.java`, `dto/response/HospedagemResponseDTO.java`, `mapper/HospedagemMapper.java`
- `exception/ReservaConflitanteException.java`, `exception/CapacidadeExcedidaException.java` (novas, 409)
- `exception/handler/GlobalExceptionHandler.java` (novas exceções → 409)
- `db/migration/V3__reservas_data_hora_modalidade.sql`, `V4__reserva_motivo_cancelamento_falta.sql` (novas)
- `application.properties` (capacidade configurável)
- `service/HospedagemServiceTest.java` (reescrito: 13 testes de reserva/capacidade/presença)

### Comandos executados e resultados reais
```
mvnw -B clean test   → EXITCODE=0 | Tests run: 53, Failures: 0, Errors: 0 | BUILD SUCCESS
  AuthSecurityRegressionTest: 4 | SecurityIntegrationTest: 9
  HospedagemServiceTest: 13 | PetServiceTest: 8 | TutorServiceTest: 13
  NormalizadorDadosTest: 5 | SystemApplicationTests: 1
```
Contagem 59→53 porque 6 testes obsoletos do comportamento antigo (`existsByPetIdAndStatusIn`,
bloqueio total de pet) foram substituídos pelos testes de sobreposição/capacidade/presença.

### Critérios de aceite atendidos
- [x] Reserva sobreposta do mesmo pet rejeitada (409, testado)
- [x] Reservas adjacentes permitidas (testado)
- [x] Capacidade esgotada rejeitada (409, testado)
- [x] Reagendamento revalida sobreposição + capacidade excluindo a própria reserva (testado)
- [x] Check-in/out com transições válidas; duplicados rejeitados (testado)
- [x] Falta/cancelamento exigem motivo (testado)
- [x] FUNCIONARIO registra presença mas não cria reservas/tutores (matriz via @PreAuthorize)
- [x] Migrations V3/V4 versionadas; V1 duplicada removida (só 1 V1 no diretório)

### Como testar manualmente
1. Como ADMIN: `POST /api/hospedagens` (modalidade HOTEL, entrada/saída futuras) → 201.
2. Repetir com sobreposição para o mesmo pet → 409.
3. Reserva adjacente (entrada == saída anterior) → 201.
4. Preencher capacidade (ou baixar env para 1) e repetir → 409.
5. Como FUNCIONARIO: `POST /api/hospedagens/{id}/check-in` → 200; repetir → 400; `POST .../check-out` → 200.
6. `POST /api/hospedagens/{id}/falta?motivo=...` e `DELETE /api/hospedagens/{id}?motivo=...` → motivo obrigatório.
7. Como FUNCIONARIO: `POST /api/hospedagens` → 403.

### Pendências e riscos remanescentes
- **V3/V4 NÃO executadas contra MySQL 8 real** (Docker indisponível neste ambiente); sintaxe
  revisada, mas `MODIFY COLUMN` + `CHECK` precisam de validação em staging antes de produção.
- **Rate-limit de login** ainda pendente (erro já genérico anti-enumeração).
- **Multi-instância:** lock é por JVM; escalar horizontalmente exige lock pessimista no banco.
- **Histórico/auditoria:** só timestamps + motivo; sem tabela de auditoria dedicada (Fase 5).
- **Financeiro/pacotes/notificações/dashboard:** escopo da Fase 5, não iniciados.

---

## RISCOS CRÍTICOS (corrigir antes de qualquer uso real)
1. **CRÍTICO — Registro público privilegiado / auto-elevação para ADMIN.**
   `SecurityConfig` faz `.requestMatchers("/api/auth/register").permitAll()` e
   `AuthController.register()` faz `usuario.setRole(dto.role())`. Qualquer pessoa
   na internet pode `POST /api/auth/register {"role":"ADMIN"}` e virar administrador.
2. **CRÍTICO — Credenciais padrão semeadas em TODOS os ambientes.**
   `DataSeeder` cria `admin@creche.com/admin123` e `func@creche.com/func123` no
   primeiro boot e **registra as senhas no log** (`log.info(...admin123...)`).
3. **ALTO — Swagger/API-docs públicos por padrão** (`SWAGGER_ENABLED=true`) e
   matchers `permitAll` para `/swagger-ui/**` e `/v3/api-docs/**`.
4. **ALTO — CORS fixo no código** (`localhost` hardcoded), não configurável por ambiente.
5. **ALTO — `ddl-auto=update` como padrão** (conflita com Flyway como fonte de verdade).
6. **MÉDIO — Sem configuração de fuso horário** (`America/Sao_Paulo` ausente).
7. **MÉDIO — CPF/telefone não normalizados** antes de persistir/verificar unicidade.
8. **MÉDIO — PII em logs** (nomes de pets/tutores, e-mails) e senha em log do seeder.
9. **MÉDIO — `.gitignore` contém `*.xml`** (ignoraria `pom.xml` e qualquer config futura).
10. **MÉDIO — UI estática expõe credenciais** (`admin@creche.com/admin123` no HTML) e usa `innerHTML` (risco de XSS).
11. **BAIXO — Sem rate-limit de login** (proteção contra força bruta pendente).
12. **BAIXO — Sem bloqueio otimista** (`@Version`) nas entidades (concorrência futura).

### Lacunas funcionais (fases seguintes)
- Check-in/check-out, presença, faltas
- Capacidade configurável + anti-overbooking concorrente
- Serviços/preços/pacotes com saldo e histórico (BigDecimal)
- Cobranças/pagamentos com histórico e anti-duplicidade
- Pertences, alertas operacionais (vacinas, restrições)
- Dashboard/relatórios, auditoria de ações
- Notificações por e-mail (templates, sem envio real sem config)
- Frontend React+TS+Vite+Tailwind (hoje só UI estática de teste)
- Reservas com data+hora (hoje hospedagem é só `LocalDate`)

---

## FASE 2 — PLANO PRIORIZADO

### Fase 3 — Segurança e fundação (EXECUTADA NESTA ENTREGA)
- Remover registro público; `/api/auth/register` passa a **ADMIN-only** + `@PreAuthorize`.
- Substituir `DataSeeder` por `AdminBootstrap` **gateado por propriedade**, sem
  credenciais padrão e sem log de senha (provisionamento seguro do 1º dono).
- Restringir Swagger por ambiente (default OFF).
- CORS por variável de ambiente (`app.cors.allowed-origins`).
- `ddl-auto=validate` por padrão; profiles `dev`/`prod`; Flyway fonte de verdade.
- Fuso `America/Sao_Paulo` (JVM + Jackson + JDBC).
- Normalização de CPF/telefone/e-mail (+ migração V2).
- Limpeza de logs (sem senha/token/PII desnecessário).
- `@EnableMethodSecurity` + matriz de permissões no backend.
- Corrigir `.gitignore` (`*.xml`) e remover credenciais da UI estática.
- Testes de regressão para cada correção.

### Fase 4 — Fluxos operacionais (roadmap)
Reservas data+hora + capacidade + anti-overbooking; check-in/out e presença;
serviços/pacotes/saldo; cobranças/pagamentos.

### Fase 5 — Testes e interface
Frontend React+TS+Vite+Tailwind; dashboard; agenda; check-in; alertas; ponta a ponta.

### Fase 6 — Implantação
HTTPS, backups/restauração testados, monitoramento, update/rollback, runbook.

### Riscos/pendências
- Rate-limit de login — melhoria futura (erro já é genérico).
- Reservas só por data — ajuste na Fase 4.
- Multi-tenant — fundação preparada, isolamento só com 2ª unidade.
