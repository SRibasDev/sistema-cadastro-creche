# Sistema de Cadastro — Tutores e Pets

## Sobre o Projeto

O Sistema de Cadastro foi criado para atender a rotina de uma creche/hotel para cachorros, facilitando o cadastro de tutores e seus pets.

O cenário funciona assim:

Quando um cliente chega na creche, a administradora realiza o cadastro das informações do tutor e do seu cão através de uma interface web. Esses dados passam por toda a arquitetura do sistema em Spring Boot até serem persistidos no banco de dados.

Além do cadastro, o sistema também permite visualizar:

- Todos os pets cadastrados;
- Seus respectivos tutores;
- Informações completas de cada registro.


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

O fluxo deve permanecer sempre unidirecional para manter a organização do projeyo.

---

# Estrutura dos Pacotes

## `controller`

Pacote responsável pelas rotas HTTP da aplicação.

### Funções principais:

- Receber requisições;
- Capturar parâmetros;
- Retornar respostas HTTP;
- Definir status corretos (`200`, `201`, `404`, etc.).

### Exemplo:

```java
@RestController
@RequestMapping("/api/pets")
public class PetController {
}
```

---

## `dto`

Contém os objetos de transferência de dados (`DTOs`).

Os DTOs evitam a exposição direta das entidades para a camada web e ajudam a manter segurança e organização.

### Divisão:

- `RequestDTO` → dados recebidos;
- `ResponseDTO` → dados enviados na resposta.

---

## `controller.mapper`

Responsável pela conversão entre DTOs e Entities.

Os mappers ajudam a:

- Reduzir acoplamento;
- Evitar repetição de código;
- Prevenir recursão infinita em relacionamentos JPA.

### Exemplo:

```java
PetMapper.toEntity(dto);
PetMapper.toResponse(entity);
```

---

## `domain.service`

Camada onde ficam as regras de negócio do sistema.

### Responsabilidades:

- Validações;
- Processamento das informações;
- Comunicação com os repositórios;
- Regras específicas da creche.

Essa é a principal camada da aplicação.

---

## `domain.entity`

Contém as entidades JPA mapeadas para o banco de dados.

### Responsável por:

- Representar tabelas;
- Definir relacionamentos;
- Configurar persistência.

### Exemplo:

```java
@Entity
public class Pet {
}
```

### Relacionamentos utilizados:

- `@OneToMany`
- `@ManyToOne`
- `@OneToOne`

---

## `domain.repository`

Interfaces responsáveis pelo acesso ao banco de dados utilizando Spring Data JPA.

### Exemplo:

```java
public interface PetRepository extends JpaRepository<Pet, Long> {
}
```

---

# Tecnologias Utilizadas

- Java 24
- Spring Boot 4.x
- Spring Web
- Spring Data JPA
- MySQL 8
- Docker
- Docker Compose
- Lombok
- Maven

---

# Como Executar o Projeto

## Pré-requisitos

Antes de iniciar, certifique-se de possuir:

- Docker Desktop instalado e ativo;
- Java 24 configurado;
- Git instalado;
- IntelliJ IDEA (recomendado).

---

# Clonando o Projeto

```bash
git clone https://github.com/SRibasDev/sistema-cadastro-creche
```

```bash
cd sistema-cadastro-creche
```

---

# Subindo o Banco de Dados

Na raiz do projeto, execute:

```bash
docker-compose up -d
```

### Importante

Na primeira execução, o MySQL pode levar entre 15 e 20 segundos para inicializar completamente. Aguarde esse tempo antes de iniciar o Spring Boot para evitar erros de conexão.

---

# Executando o Spring Boot

1. Abra o projeto no IntelliJ IDEA;
2. Aguarde o Maven baixar as dependências do `pom.xml`;
3. Execute a classe:

```text
SystemApplication.java
```

A aplicação estará disponível em:

```text
http://localhost:8080
```

---

# Fluxo de Trabalho Git

Para manter a branch `main` sempre estável, o projeto utiliza o padrão de Feature Branches.

Nunca realize commits diretamente na `main`.

---

## Atualizando a Main

```bash
git checkout main
```

```bash
git pull origin main
```

---

## Criando uma Nova Feature

```bash
git checkout -b feature/nome-do-recurso
```

### Exemplos

```bash
feature/cadastro-tutor
feature/busca-pet
feature/agendamento-online
```

---

## Finalizando uma Feature

Adicionar alterações:

```bash
git add .
```

Realizar commit:

```bash
git commit -m "feat: implementa service de cadastro de tutor"
```

Enviar para o GitHub:

```bash
git push origin feature/nome-do-recurso
```

Após isso, deve ser aberto um Pull Request para revisão antes do merge na `main`.

---

# Endpoints da API

## Pets

### Cadastrar Pet

```http
POST /api/pets/tutor/{tutorId}
```

Responsável por cadastrar um novo pet vinculado a um tutor existente.

---

### Listar Pets

```http
GET /api/pets
```

Retorna todos os pets cadastrados formatados via DTO.

---

# Visão Futura do Sistema

O projeto foi estruturado pensando em crescimento e escalabilidade.

### Funcionalidades planejadas:

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

Esse sistema representa a base tecnológica da Mundo Particular Lar e foi pensado para crescer junto com o negócio.
