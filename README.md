# Sistema de Cadastro — Tutores e Pets

## Sobre o Projeto

O Sistema de Cadastro foi criado para atender a rotina de uma creche/hotel para cachorros, facilitando o cadastro de tutores e seus pets.

O cenário funciona assim:

Quando um cliente chega na creche, a administradora realiza o cadastro das informações do tutor e do seu cão através de uma interface web. Esses dados passam por toda a arquitetura do sistema em Spring Boot até serem persistidos no banco de dados.

Além do cadastro, o sistema também permite:

- Cadastrar, buscar, atualizar e deletar tutores;
- Cadastrar, buscar, atualizar e deletar pets;
- Listar todos os pets de um tutor específico.

---

# Arquitetura do Sistema

O projeto segue o padrão de Arquitetura em Camadas, mantendo separação clara de responsabilidades e um fluxo de dados organizado.

```text
[Mundo Externo / Front-end / Postman]
                │
                ▼
CONTROLLER (Camada HTTP / Rotas)
                │
                ▼
DTO + MAPPER (Conversão de Dados)
                │
                ▼
SERVICE (Regras de Negócio)
                │
                ▼
REPOSITORY (Acesso ao Banco)
                │
                ▼
DATABASE (MySQL)
```

O fluxo deve permanecer sempre unidirecional para manter a organização do projeto.

---

# Estrutura dos Pacotes

## `controller`

Pacote responsável pelas rotas HTTP da aplicação.

### Funções principais:

- Receber requisições;
- Capturar parâmetros;
- Retornar respostas HTTP;
- Definir status corretos (`200`, `201`, `204`, `404`, etc.).

---

## `dto`

Contém os objetos de transferência de dados (`DTOs`).

Os DTOs evitam a exposição direta das entidades para a camada web e ajudam a manter segurança e organização.

### Divisão:

- `RequestDTO` → dados recebidos com validações (`@NotBlank`, `@CPF`, etc.);
- `ResponseDTO` → dados enviados na resposta, sem expor campos sensíveis como CPF.

---

## `mapper`

Responsável pela conversão entre DTOs e entidades.

Os mappers ajudam a:

- Reduzir acoplamento;
- Evitar repetição de código;
- Manter a separação entre camadas.

### Métodos:

- `toEntity(dto)` → converte RequestDTO em entidade;
- `toResponseDTO(entity)` → converte entidade em ResponseDTO;
- `updateEntityFromDTO(dto, entity)` → atualiza entidade existente com dados do DTO.

---

## `domain.service`

Camada onde ficam as regras de negócio do sistema.

### Responsabilidades:

- Validações de negócio (CPF e telefone duplicados);
- Processamento das informações;
- Comunicação com os repositórios;
- Lançamento de exceções customizadas.

---

## `domain.entity`

Contém as entidades JPA mapeadas para o banco de dados.

### Relacionamentos:

- `Tutor` possui `@OneToMany` com `Pet`;
- `Pet` possui `@ManyToOne` com `Tutor`.

---

## `domain.repository`

Interfaces que estendem `JpaRepository` para acesso ao banco de dados.

### Métodos customizados:

- `PetRepository.findByTutorId(Long tutorId)` → lista pets por tutor;
- `TutorRepository.existsByCpf(String cpf)` → verifica CPF duplicado;
- `TutorRepository.existsByTelefone(String telefone)` → verifica telefone duplicado.

---

## `exception`

Contém as exceções customizadas e o handler global de erros.

- `PetNotFoundException` → lançada quando um pet não é encontrado (`404`);
- `TutorNotFoundException` → lançada quando um tutor não é encontrado (`404`);
- `BusinessException` → lançada quando uma regra de negócio é violada (`400`);
- `GlobalExceptionHandler` → captura todas as exceções e retorna respostas JSON padronizadas.

---

# Tecnologias Utilizadas

- Java 21
- Spring Boot 3.4.1
- Spring Web
- Spring Data JPA
- Hibernate Validator
- Springdoc OpenAPI (Swagger UI)
- MySQL 8
- Docker
- Docker Compose
- Lombok
- Maven

---

# Como Executar o Projeto

## Pré-requisitos

- Docker Desktop instalado e ativo;
- Java 21 configurado;
- Git instalado;
- IntelliJ IDEA (recomendado).

---

## Clonando o Projeto

```bash
git clone https://github.com/SRibasDev/sistema-cadastro-creche
cd sistema-cadastro-creche
```

---

## Configurando as Variáveis de Ambiente

Crie um arquivo `.env` na raiz do projeto baseado no `.env.example`:

```env
DB_URL=jdbc:mysql://localhost:3306/creche_pet_db
DB_USERNAME=root
DB_PASSWORD=sua_senha
```

> O arquivo `.env` está no `.gitignore` e nunca deve ser commitado.

Configure as variáveis no IntelliJ em `Edit Configurations → Environment Variables`.

---

## Subindo o Banco de Dados

```bash
docker-compose up -d
```

Na primeira execução, aguarde 15 a 20 segundos antes de iniciar o Spring Boot.

---

## Executando o Spring Boot

1. Abra o projeto no IntelliJ IDEA;
2. Aguarde o Maven baixar as dependências;
3. Execute a classe `SystemApplication.java`.

A aplicação estará disponível em:

```
http://localhost:8080
```

A documentação Swagger estará disponível em:

```
http://localhost:8080/swagger-ui/index.html
```

---

# Endpoints da API

## Tutores

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| `POST` | `/api/tutores` | Cadastrar tutor | `201` |
| `GET` | `/api/tutores` | Listar todos os tutores | `200` |
| `GET` | `/api/tutores/{id}` | Buscar tutor por ID | `200` |
| `PUT` | `/api/tutores/{id}` | Atualizar tutor | `200` |
| `DELETE` | `/api/tutores/{id}` | Deletar tutor | `204` |

## Pets

| Método | Endpoint | Descrição | Status |
|--------|----------|-----------|--------|
| `POST` | `/api/pets/{tutorId}` | Cadastrar pet vinculado a um tutor | `201` |
| `GET` | `/api/pets` | Listar todos os pets | `200` |
| `GET` | `/api/pets/{petId}` | Buscar pet por ID | `200` |
| `GET` | `/api/pets/tutor/{tutorId}` | Listar pets de um tutor | `200` |
| `PUT` | `/api/pets/{petId}` | Atualizar pet | `200` |
| `DELETE` | `/api/pets/{petId}` | Deletar pet | `204` |

---

# Exemplos de Requisição

## Cadastrar Tutor

```json
POST /api/tutores
{
  "nome": "João Silva",
  "telefone": "11999999999",
  "cpf": "529.982.247-25"
}
```

## Cadastrar Pet

```json
POST /api/pets/1
{
  "nome": "Rex",
  "raca": "Labrador",
  "dataNascimento": "2020-03-15"
}
```

---

# Tratamento de Erros

Todas as respostas de erro seguem o padrão:

```json
{
  "status": 404,
  "erro": "Recurso não encontrado",
  "mensagem": "Pet não encontrado com o id: 99",
  "timestamp": "2024-06-03T14:32:10"
}
```

---

# Fluxo de Trabalho Git

Para manter a branch `main` sempre estável, o projeto utiliza o padrão de Feature Branches.

Nunca realize commits diretamente na `main`.

## Criando uma Nova Feature

```bash
git checkout main
git pull origin main
git checkout -b feature/nome-do-recurso
```

## Finalizando uma Feature

```bash
git add .
git commit -m "feat: descrição do que foi implementado"
git push origin feature/nome-do-recurso
```

Após isso, abra um Pull Request para revisão antes do merge na `main`.

---

# Funcionalidades Planejadas

- Sistema de agendamentos online;
- Controle de hospedagem;
- Agenda em tempo real;
- Área do cliente;
- Integração com pagamentos;
- Dashboard administrativo;
- Histórico dos pets;
- Controle de vacinas;
- Integração com IA para automações futuras.

---

# Considerações Finais

O objetivo do projeto não é apenas funcionar, mas também seguir boas práticas de arquitetura e desenvolvimento profissional.

Toda nova funcionalidade deve respeitar:

- Separação de responsabilidades;
- Arquitetura em camadas;
- Código limpo;
- Baixo acoplamento;
- Facilidade de manutenção;
- Escalabilidade futura.