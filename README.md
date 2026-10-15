# Sistema de Cadastro — Creche de Pets

API REST desenvolvida em Java com Spring Boot para gerenciar o dia a dia de uma creche para cachorros. O sistema cuida do cadastro de tutores, pets e hospedagens, com autenticação por JWT e controle de acesso por perfil de usuário.

---

## O que o sistema faz

- Cadastro completo de tutores e pets com validações e normalização de CPF/telefone
- Reservas de **creche** e **hotel** com data/hora de entrada e saída (`LocalDateTime`)
- **Anti-conflito**: o mesmo pet não pode ter reservas sobrepostas (intervalos adjacentes são permitidos)
- **Capacidade configurável por modalidade/dia**, com proteção contra overbooking em reservas concorrentes
- Presença operacional: **check-in**, **check-out** e **falta com motivo** (acessível a FUNCIONARIO)
- Cancelamento com **motivo obrigatório** (mín. 3 caracteres) e regra de negócio no banco (CHECK)
- Exclusão lógica — os dados nunca somem do banco, apenas ficam inativos
- Listagem paginada para não sobrecarregar o servidor (máx. 100 por página)
- Autenticação via JWT com dois perfis: ADMIN e FUNCIONARIO (matriz de permissões abaixo)
- Respostas de erro padronizadas em toda a API

---

## Tecnologias

- Java 21
- Spring Boot 3.4.1
- Spring Security + JWT (JJWT 0.12.3)
- Spring Data JPA + Hibernate
- H2 (banco em **arquivo**, para rodar sem MySQL/Docker)
- Flyway (opcional; desativado no H2)
- Lombok
- Springdoc OpenAPI (Swagger)
- Actuator + Prometheus
- JUnit 5 + Mockito

---

## Como rodar o projeto

### Pré-requisitos

- Java 21
- Git

### 1. Clone o repositório

```bash
git clone https://github.com/SRibasDev/sistema-cadastro-creche
cd sistema-cadastro-creche
```

### 2. Configure as variáveis de ambiente

Copie o `.env.example` como modelo (ou crie um `.env`):

```env
# Banco H2 (em arquivo, sem MySQL/Docker)
# O caminho de dados padrão é: ./data/creche-db

# JWT (pode ser qualquer valor com pelo menos 32 caracteres em dev/local)
JWT_SECRET=uma_chave_secreta_com_pelo_menos_32_caracteres

# Opcional (padrões abaixo)
JWT_EXPIRATION=3600000
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=dev
CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000,http://localhost:8080
APP_CRECHE_CAPACIDADE_CRECHE=10
APP_CRECHE_CAPACIDADE_HOTEL=20
```

> `JWT_SECRET` precisa ter **no mínimo 32 caracteres** para uso em produção. Em dev local, um valor menor pode ser usado para testes (veja `application-h2.properties` como referência).

### 3. Rode a aplicação

#### Via Maven Wrapper (recomendado)

```bash
./mvnw.cmd spring-boot:run
```

#### Via Maven Wrapper (recomendado)

```bash
./mvnw.cmd spring-boot:run
```

#### Via IDE

Execute a classe `SystemApplication.java` pelo IntelliJ/VS Code.

> **Banco:** a aplicação usa **H2 em arquivo** (`./data/creche-db`) por padrão. Não há MySQL/Docker necessário.
>
> **MySQL:** se quiser usar MySQL, defina `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER_CLASS=com.mysql.cj.jdbc.Driver` e `DDL_AUTO=validate` nas variáveis de ambiente. A aplicação usará o MySQL nesse caso.

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html` (apenas no perfil `dev` e com token de ADMIN)
- Interface de testes (UI estática): `http://localhost:8080/`
- Health check: `http://localhost:8080/actuator/health` (público)

> **Banco:** a aplicação usa **H2 em arquivo** (`./data/creche-db`) por padrão. Não há MySQL/Docker necessário.
>
> **MySQL:** se quiser usar MySQL, defina `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER_CLASS=com.mysql.cj.jdbc.Driver` e `DDL_AUTO=validate` nas variáveis de ambiente. A aplicação usará o MySQL nesse caso.

## Autenticação

Todos os endpoints de dados são protegidos. Primeiro faça login para receber o token.

### Provisionamento do 1º dono (ADMIN)

Não existe registro público de usuários. O primeiro ADMIN é criado de forma explícita
e controlada (sem credenciais padrão no código):

1. Defina no ambiente, **uma única vez**, no primeiro deploy:

   ```env
   APP_BOOTSTRAP_ADMIN_ENABLED=true
   APP_BOOTSTRAP_ADMIN_EMAIL=dono@creche.com
   APP_BOOTSTRAP_ADMIN_PASSWORD=<senha_forte_min_12_caracteres>
   APP_BOOTSTRAP_ADMIN_NOME=Dono
   ```

2. Suba a aplicação. Ela cria o ADMIN apenas se a tabela de usuários estiver vazia
   (recusa senha fraca e não registra a senha em log).
3. Faça login, troque a senha e volte `APP_BOOTSTRAP_ADMIN_ENABLED=false`.

> Demais usuários (funcionários) são criados por um ADMIN autenticado via
> `POST /api/auth/register`. Não há auto-elevação de permissões.

```json
POST /api/auth/login
{
  "email": "dono@creche.com",
  "senha": "<senha_forte_min_12_caracteres>"
}
```

Resposta:

```json
{
  "token": "eyJ...",
  "nome": "Dono",
  "role": "ADMIN"
}
```

Use o token nas requisições seguintes no header:

```
Authorization: Bearer eyJ...
```

### Cadastrar novo usuário (somente ADMIN)

```
POST /api/auth/register
Authorization: Bearer <token de um ADMIN>
```

```json
{
  "nome": "Novo Funcionário",
  "email": "novo@creche.com",
  "senha": "Senha_segura_123",
  "role": "FUNCIONARIO"
}
```

Resposta: `201 Created`. Se o e-mail já estiver em uso, retorna `409 Conflict`.
A senha deve ter no mínimo 8 caracteres, com maiúsculas, minúsculas e números.
Quem não for ADMIN recebe `401` (sem token) ou `403` (token de funcionário).

### Permissões por perfil

Validação no **backend** (rota + `@PreAuthorize`); esconder botões no frontend não basta.

| Ação / Recurso                  | ADMIN | FUNCIONARIO |
|--------------------------------|-------|-------------|
| GET (leitura de dados)         | Sim   | Sim         |
| POST/PUT/PATCH (cadastros)     | Sim   | Sim*        |
| DELETE (exclusão lógica)       | Sim   | Não         |
| POST `/api/auth/register`      | Sim   | Não         |
| Swagger (`/v3/api-docs`)       | Sim   | Não         |
| Actuator (exceto health)       | Sim   | Não         |

> \* Funcionário registra presença/operações do dia a dia. Ações administrativas
> restritas (cadastro/exclusão de usuários, gestão financeira) serão refinadas
> com `@PreAuthorize` por módulo nas próximas fases.


---

## Endpoints

### Autenticacao

| Metodo | Endpoint | Descricao |
|--------|----------|-----------|
| POST | `/api/auth/login` | Fazer login e receber o token |
| POST | `/api/auth/register` | Cadastrar novo usuário (ADMIN ou FUNCIONARIO) |

### Tutores

| Metodo | Endpoint | Descricao | Status |
|--------|----------|-----------|--------|
| POST | `/api/tutores` | Cadastrar tutor | 201 |
| GET | `/api/tutores` | Listar tutores ativos (paginado) | 200 |
| GET | `/api/tutores/{id}` | Buscar tutor por ID | 200 |
| PUT | `/api/tutores/{id}` | Atualizar tutor | 200 |
| DELETE | `/api/tutores/{id}` | Inativar tutor (soft delete) | 204 |

### Pets

| Metodo | Endpoint | Descricao | Status |
|--------|----------|-----------|--------|
| POST | `/api/pets/{tutorId}` | Cadastrar pet vinculado a um tutor | 201 |
| GET | `/api/pets` | Listar pets ativos (paginado) | 200 |
| GET | `/api/pets/{petId}` | Buscar pet por ID | 200 |
| GET | `/api/pets/tutor/{tutorId}` | Listar todos os pets de um tutor | 200 |
| PUT | `/api/pets/{petId}` | Atualizar pet | 200 |
| DELETE | `/api/pets/{petId}` | Inativar pet (soft delete) | 204 |

### Hospedagens

| Metodo | Endpoint | Descricao | Status |
|--------|----------|-----------|--------|
| POST | `/api/hospedagens` | Criar hospedagem | 201 |
| GET | `/api/hospedagens` | Listar hospedagens (paginado) | 200 |
| GET | `/api/hospedagens/{id}` | Buscar hospedagem por ID | 200 |
| GET | `/api/hospedagens/pet/{petId}` | Listar hospedagens de um pet | 200 |
| PUT | `/api/hospedagens/{id}` | Atualizar dados da hospedagem | 200 |
| PATCH | `/api/hospedagens/{id}/status` | Atualizar status da hospedagem | 200 |
| DELETE | `/api/hospedagens/{id}` | Cancelar hospedagem | 204 |

> **Paginação:** `GET /api/tutores`, `/api/pets` e `/api/hospedagens` aceitam `?page=0&size=10&sort=id,asc`. O tamanho máximo de página é **100**.
>
> **Reserva ≠ presença.** Criar a reserva (`POST`) não coloca o pet na creche — é preciso
> fazer check-in (`POST /{id}/check-in`) no dia, e check-out (`POST /{id}/check-out`) na saída.
> Faltas são registradas com motivo (`POST /{id}/falta?motivo=...`).
>
> **Sobreposição:** o mesmo pet não pode ter duas reservas ativas (`AGENDADA`/`EM_ANDAMENTO`)
> com intervalos sobrepostos. Intervalos **adjacentes** (saída == entrada) são **permitidos**.
> Resposta: `409 Conflict`.
>
> **Capacidade:** cada modalidade tem um limite de vagas **por dia de calendário**
> (`APP_CRECHE_CAPACIDADE_CRECHE` / `APP_CRECHE_CAPACIDADE_HOTEL`, padrões 10/20).
> A verificação percorre todos os dias do intervalo; reservas concorrentes disputando a
> última vaga são serializadas (lock + transação) — uma passa, a outra recebe `409`.
>
> **Regras válidas para ambos os perfis (quando autorizados):** entrada deve ser presente/futura,
> saída deve ser futura e posterior à entrada.

---

## Exemplos de requisicao

### Cadastrar tutor

```json
POST /api/tutores

{
  "nome": "João Silva",
  "telefone": "11999999999",
  "cpf": "529.982.247-25"
}
```

### Cadastrar pet

```json
POST /api/pets/1

{
  "nome": "Rex",
  "raca": "Labrador",
  "dataNascimento": "2020-03-15"
}
```

### Criar reserva (modalidade + data/hora)

```json
POST /api/hospedagens

{
  "petId": 1,
  "modalidade": "CRECHE",
  "dataEntrada": "2026-10-13T08:00:00",
  "dataSaida": "2026-10-13T18:00:00",
  "observacoes": "Pet alérgico a frango"
}
```

> `modalidade`: `CRECHE` ou `HOTEL`. Adjacência é permitida (saída == próxima entrada);
> sobreposição do mesmo pet retorna `409`. Capacidade cheia também retorna `409`.

### Reagendar reserva (ADMIN)

```json
PUT /api/hospedagens/1

{
  "petId": 1,
  "modalidade": "CRECHE",
  "dataEntrada": "2026-10-14T08:00:00",
  "dataSaida": "2026-10-14T18:00:00",
  "observacoes": "Remarcado a pedido do tutor"
}
```

### Presença (ADMIN ou FUNCIONARIO)

```
POST /api/hospedagens/1/check-in
POST /api/hospedagens/1/check-out
POST /api/hospedagens/1/falta?motivo=Tutor%20avisou%20que%20o%20pet%20est%C3%A1%20doente
```

### Cancelar reserva (ADMIN, motivo obrigatório)

```
DELETE /api/hospedagens/1?motivo=Tutor%20desistiu%20da%20viagem
```

### Atualizar status da hospedagem (ADMIN)

```
PATCH /api/hospedagens/1/status?status=EM_ANDAMENTO
```

---

## Banco de dados e migrações (Flyway)

O schema é versionado — o Hibernate **valida** (`ddl-auto=validate`), nunca altera o banco.
As migrações ficam em `src/main/resources/db/migration/` e rodam automaticamente na subida:

| Versão | O que faz |
|--------|-----------|
| V1 | Schema inicial (tutores, pets, hospedagens, usuários) |
| V2 | Normaliza CPF/telefone existentes (só dígitos) |
| V3 | Reservas com data/hora (`DATETIME`), coluna `modalidade` (`CRECHE`/`HOTEL`, default `HOTEL`), CHECK de intervalo e índices de sobreposição/capacidade |
| V4 | `motivo_cancelamento` + status `FALTA`, com CHECK exigindo motivo (mín. 3 chars) em `CANCELADA`/`FALTA` |

> Bases existentes (V1) são migradas automaticamente: datas viram meia-noite e a modalidade
> assume `HOTEL`. Para mudar a modalidade de registros antigos, edite via API (ADMIN).

---

## Testes

```bash
.\mvnw.cmd -B clean test
```

Última execução verificada: **53 testes, 0 falhas, 0 erros** (`BUILD SUCCESS`).
Cobrem regressão de segurança (registro anônimo→401, FUNCIONARIO→403, Swagger restrito),
normalização de CPF/telefone, CRUD de tutores/pets e regras de reserva (sobreposição,
capacidade, check-in/out, transições de status, motivo obrigatório).

---

## Padrão de erro

Todas as respostas de erro seguem o mesmo formato:

```json
{
  "status": 404,
  "erro": "Recurso não encontrado",
  "mensagem": "Pet com ID 99 não encontrado.",
  "timestamp": "2025-06-27T14:32:10"
}
```

---

## Arquitetura

O projeto segue arquitetura em camadas com responsabilidades bem separadas:

```
Controller  →  recebe a requisição HTTP
DTO         →  valida e transporta os dados
Mapper      →  converte DTO em entidade e vice-versa
Service     →  aplica as regras de negócio
Repository  →  fala com o banco de dados
```

O fluxo é sempre unidirecional. O controller não acessa o repository diretamente, e a entidade nunca chega ao controller.

---

## Estrutura de pastas

```
src/main/java/com/particaolar/mundo/system/
├── config/              # SecurityConfig, AdminBootstrap, TransactionConfig
├── controller/          # PetController, TutorController, HospedagemController,
│                        # ServicoController, CobrancaController, DashboardController, RelatorioController
├── domain/
│   ├── entity/          # Pet, Tutor, Hospedagem, Servico, Cobranca, Pagamento, AuditLog
│   ├── repository/      # PetRepository, TutorRepository, HospedagemRepository,
│   │                    # ServicoRepository, CobrancaRepository, PagamentoRepository, AuditLogRepository
│   └── service/         # PetService, TutorService, HospedagemService,
│                        # ServicoService, CobrancaService, DashboardService, RelatorioService, AuditLogService
├── dto/
│   ├── request/         # DTOs de entrada com Bean Validation
│   └── response/        # DTOs de saída
├── enums/               # StatusHospedagem, Role, Porte, TipoServico, UnidadeCobranca, StatusCobranca, FormaPagamento
├── exception/           # Exceções de domínio e negócio (400, 404, 409)
│   └── handler/         # GlobalExceptionHandler, ErrorResponse
├── mapper/              # PetMapper, TutorMapper, HospedagemMapper, ServicoMapper, CobrancaMapper
└── security/            # JWT, filtros e autenticação
    ├── controller/      # AuthController, UsuarioController
    ├── dto/             # LoginRequestDTO, LoginResponseDTO, RegisterRequestDTO, UsuarioResponseDTO, etc.
    ├── entity/          # Usuario (UserDetails com isEnabled() vinculado a status ativo)
    ├── filter/          # JwtAuthFilter
    ├── repository/      # UsuarioRepository
    └── service/         # JwtService, UsuarioService
```

Além do código Java, o projeto contém:

- `src/main/resources/static/` — interface web completa e responsiva servida em `/`
- `src/main/resources/db/migration/` — migrações de banco do Flyway (V1 a V7)
- `src/test/` — suite de testes automatizados com JUnit 5, Mockito e MockMvc (85 testes, 0 falhas)

---

## Módulos e Endpoints da API

### Autenticação & Usuários
- `POST /api/auth/login` — Login JWT (público)
- `POST /api/auth/register` — Cadastro de usuário (ADMIN)
- `GET /api/usuarios` — Listar funcionários e usuários (ADMIN)
- `GET /api/usuarios/{id}` — Detalhes de usuário (ADMIN)
- `PUT /api/usuarios/{id}` — Atualizar usuário (ADMIN)
- `PATCH /api/usuarios/{id}/status?ativo={true|false}` — Ativar/desativar usuário (ADMIN)
- `PATCH /api/usuarios/{id}/senha` — Redefinir senha de usuário

### Tutores & Pets
- `GET /api/tutores` — Listar tutores paginados
- `POST /api/tutores` — Cadastrar tutor (ADMIN)
- `PUT /api/tutores/{id}` — Atualizar tutor (ADMIN)
- `DELETE /api/tutores/{id}` — Desativação lógica de tutor (ADMIN)
- `GET /api/pets` — Listar pets paginados
- `POST /api/pets/{tutorId}` — Cadastrar pet com dados de saúde e vacinas (ADMIN)
- `PUT /api/pets/{petId}` — Atualizar pet (ADMIN)
- `DELETE /api/pets/{petId}` — Desativação lógica de pet (ADMIN)

### Serviços & Tarifas
- `GET /api/servicos` — Listar serviços ativos do catálogo
- `GET /api/servicos/todos` — Listar todos os serviços (ADMIN)
- `POST /api/servicos` — Cadastrar serviço (ADMIN)
- `PUT /api/servicos/{id}` — Atualizar serviço (ADMIN)
- `PATCH /api/servicos/{id}/status?ativo={true|false}` — Ativar/desativar serviço (ADMIN)

### Reservas & Presença
- `GET /api/hospedagens` — Listar reservas paginadas
- `POST /api/hospedagens` — Criar reserva com congelamento de preço acordado (ADMIN)
- `POST /api/hospedagens/{id}/check-in` — Registrar check-in real do pet (FUNCIONARIO / ADMIN)
- `POST /api/hospedagens/{id}/check-out` — Registrar check-out real do pet (FUNCIONARIO / ADMIN)
- `POST /api/hospedagens/{id}/falta?motivo=...` — Registrar falta com motivo obrigatório
- `DELETE /api/hospedagens/{id}?motivo=...` — Cancelar reserva com motivo obrigatório (ADMIN)

### Financeiro
- `GET /api/cobrancas` — Listar cobranças com filtro opcional por status
- `POST /api/cobrancas` — Emitir fatura com desconto e vencimento (ADMIN)
- `POST /api/cobrancas/{id}/pagamentos` — Registrar recebimento (PIX, Dinheiro, Cartão, Transferência)
- `DELETE /api/cobrancas/{id}?motivo=...` — Cancelar fatura sem pagamentos (ADMIN)

### Dashboard & Relatórios
- `GET /api/dashboard` — Métricas operacionais em tempo real e ocupação
- `GET /api/relatorios/financeiro?inicio=...&fim=...` — Relatório de faturamento e recebimentos (ADMIN)
- `GET /api/relatorios/reservas?inicio=...&fim=...` — Relatório de reservas do período

---

## Status dos Testes Automatizados

Build validado com sucesso via Maven Surefire:
- **Total de testes:** 85
- **Falhas:** 0
- **Erros:** 0
- **Cobertura:** Autenticação, regressões de segurança, gestão de usuários, tutores, pets, capacidade concorrente de creche/hotel, check-in/out, falta, cancelamento, catálogo de serviços, emissão de cobranças, controle de saldo e dashboard.