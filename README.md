# Sistema de Cadastro — Creche de Pets

API REST desenvolvida em Java com Spring Boot para gerenciar o dia a dia de uma creche para cachorros. O sistema cuida do cadastro de tutores, pets e hospedagens, com autenticação por JWT e controle de acesso por perfil de usuário.

---

## O que o sistema faz

- Cadastro completo de tutores e pets com validações
- Controle de hospedagens com status (Agendada, Em andamento, Concluída, Cancelada)
- Exclusão lógica — os dados nunca somem do banco, apenas ficam inativos
- Listagem paginada para não sobrecarregar o servidor
- Autenticação via JWT com dois perfis: ADMIN e FUNCIONARIO
- Respostas de erro padronizadas em toda a API

---

## Tecnologias

- Java 21
- Spring Boot 3.4.1
- Spring Security + JWT (JJWT 0.12.3)
- Spring Data JPA + Hibernate
- Flyway (migrações de banco)
- MySQL 8
- H2 (banco em memória para testes)
- Docker e Docker Compose
- Lombok
- Springdoc OpenAPI (Swagger)
- Actuator + Prometheus
- JUnit 5 + Mockito

---

## Como rodar o projeto

### Pré-requisitos

- Java 21
- Docker Desktop instalado e rodando
- Git

### 1. Clone o repositório

```bash
git clone https://github.com/SRibasDev/sistema-cadastro-creche
cd sistema-cadastro-creche
```

### 2. Crie o arquivo `.env` na raiz do projeto

Copie o `.env.example` como modelo. O `JWT_SECRET` precisa ter **no mínimo 32 caracteres** (256 bits):

```env
# MySQL (usado pelo docker-compose)
MYSQL_ROOT_PASSWORD=sua_senha_root
MYSQL_DATABASE=creche_pet_db
MYSQL_USER=app_user
MYSQL_PASSWORD=sua_senha_aqui

# Conexão local (modo desenvolvimento)
DB_URL=jdbc:mysql://localhost:3306/creche_pet_db
DB_USERNAME=app_user
DB_PASSWORD=sua_senha_aqui

# JWT
JWT_SECRET=uma_chave_secreta_com_pelo_menos_32_caracteres
JWT_EXPIRATION=3600000

# Servidor / ferramentas
SERVER_PORT=8080
SWAGGER_ENABLED=true
```

### 3. Suba a aplicação + banco (Docker)

```bash
docker-compose up --build
```

O compose sobe o banco MySQL (com healthcheck) e a API. As migrações do banco (Flyway) são aplicadas automaticamente na subida.

### 4. Rodando em desenvolvimento (IntelliJ)

Suba apenas o banco, se preferir:

```bash
docker-compose up db -d
```

Depois vá em `Run > Edit Configurations > SystemApplication > Environment Variables` e configure:

```
DB_URL=jdbc:mysql://localhost:3306/creche_pet_db
DB_USERNAME=app_user
DB_PASSWORD=sua_senha_aqui
JWT_SECRET=uma_chave_secreta_com_pelo_menos_32_caracteres
JWT_EXPIRATION=3600000
SERVER_PORT=8080
SWAGGER_ENABLED=true
```

### 5. Rode a aplicação

Execute a classe `SystemApplication.java` pelo IntelliJ.

- API: `http://localhost:8080`
- Swagger: `http://localhost:8080/swagger-ui/index.html`
- Interface de testes (UI estática): `http://localhost:8080/`
- Health check: `http://localhost:8080/actuator/health` (público)

---

## Autenticação

Todos os endpoints são protegidos. Primeiro faça login para receber o token.

No primeiro boot, o `DataSeeder` cria dois usuários padrão:

- `admin@creche.com` / `admin123` — perfil **ADMIN**
- `func@creche.com` / `func123` — perfil **FUNCIONARIO**

> Em ambiente real, remova o `DataSeeder` ou troque as senhas padrão.

```
POST /api/auth/login
```

```json
{
  "email": "admin@creche.com",
  "senha": "admin123"
}
```

Resposta:

```json
{
  "token": "eyJ...",
  "nome": "Admin",
  "role": "ADMIN"
}
```

Use o token nas requisições seguintes no header:

```
Authorization: Bearer eyJ...
```

### Cadastrar novo usuário

```
POST /api/auth/register
```

```json
{
  "nome": "Novo Funcionário",
  "email": "novo@creche.com",
  "senha": "senha_segura_123",
  "role": "FUNCIONARIO"
}
```

Resposta: `201 Created`. Se o e-mail já estiver em uso, retorna `409 Conflict`. A senha deve ter no mínimo 8 caracteres.

### Permissões por perfil

| Acao | ADMIN | FUNCIONARIO |
|------|-------|-------------|
| GET | Sim | Sim |
| POST | Sim | Sim |
| PUT | Sim | Sim |
| PATCH | Sim | Sim |
| DELETE | Sim | Nao |

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
> **Status da hospedagem:** além dos dados, o status também é validado — uma hospedagem `CONCLUIDA` ou `CANCELADA` não pode ser alterada, e só são permitidas transições válidas (`AGENDADA` → `EM_ANDAMENTO`/`CANCELADA`, `EM_ANDAMENTO` → `CONCLUIDA`/`CANCELADA`).

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

### Criar hospedagem

```json
POST /api/hospedagens

{
  "petId": 1,
  "dataEntrada": "2025-07-01",
  "dataSaida": "2025-07-05",
  "observacoes": "Pet alérgico a frango"
}
```

### Atualizar status da hospedagem

```
PATCH /api/hospedagens/1/status?status=EM_ANDAMENTO
```

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
├── config/              # SecurityConfig, OpenApiConfig, DataSeeder
├── controller/          # PetController, TutorController, HospedagemController
├── domain/
│   ├── entity/          # Pet, Tutor, Hospedagem
│   ├── repository/      # PetRepository, TutorRepository, HospedagemRepository
│   └── service/         # PetService, TutorService, HospedagemService
├── dto/
│   ├── request/         # DTOs de entrada com validações
│   └── response/        # DTOs de saída
├── enums/               # StatusHospedagem, Role, Porte
├── exception/           # Exceções customizadas
│   └── handler/         # GlobalExceptionHandler, ErrorResponse
├── mapper/              # PetMapper, TutorMapper, HospedagemMapper
└── security/            # JWT, filtros e autenticação
    ├── controller/      # AuthController
    ├── dto/             # LoginRequestDTO, LoginResponseDTO, RegisterRequestDTO
    ├── entity/          # Usuario
    ├── filter/          # JwtAuthFilter
    ├── repository/      # UsuarioRepository
    └── service/         # JwtService, UsuarioService
```

Além do código Java, o projeto também contém:

- `src/main/resources/static/` — interface web estática (HTML/CSS/JS) servida em `/`
- `src/main/resources/db/migration/` — migrações de banco do Flyway
- `src/test/` — testes unitários (Mockito) e de integração de segurança (H2 + MockMvc)

---

## Funcionalidades planejadas

- Agendamentos online
- Área do cliente
- Controle de vacinas
- Histórico completo dos pets
- Integração com pagamentos
- Dashboard administrativo