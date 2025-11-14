# API RESTful para Gerenciamento de Tarefas - JTech

![Jtech Logo](http://www.jtech.com.br/wp-content/uploads/2015/06/logo.png)

## Visão Geral do Projeto

API RESTful desenvolvida para gerenciamento de tarefas (tasks), permitindo operações CRUD completas. A aplicação foi construída seguindo os princípios de **Clean Architecture**, garantindo separação de responsabilidades, testabilidade e manutenibilidade do código.

**Objetivos principais:**
- Fornecer endpoints RESTful para criação, leitura, atualização e exclusão de tarefas
- Implementar validações de negócio (ex: título único case-insensitive)
- Garantir arquitetura escalável e de fácil manutenção
- Facilitar integração com frontend através de documentação Swagger/OpenAPI

## Stack Utilizada

### Backend
- **Java 21** - Linguagem de programação
- **Spring Boot 3.5.5** - Framework para desenvolvimento de aplicações Java
- **Spring Data JPA** - Abstração para acesso a dados
- **Hibernate** - ORM (Object-Relational Mapping)
- **PostgreSQL 15** - Banco de dados relacional
- **H2 Database** - Banco de dados em memória para testes

### Ferramentas e Bibliotecas
- **Gradle 8.5** - Gerenciador de dependências e build
- **Lombok** - Redução de boilerplate code
- **SpringDoc OpenAPI 2.0.4** - Documentação automática da API (Swagger)
- **Spring Boot Actuator** - Monitoramento e métricas da aplicação
- **Hibernate Validator** - Validação de dados

### Infraestrutura
- **Docker & Docker Compose** - Containerização e orquestração
- **Maven** - Publicação de artefatos (opcional)

## Como Rodar Localmente

### Pré-requisitos

- **Java 21** instalado e configurado
- **Docker** e **Docker Compose** instalados
- **Gradle** (opcional, o projeto inclui Gradle Wrapper)

### Opção 1: Usando Docker (Recomendado)

#### Passo 1: Construir o JAR

```bash
# Na raiz do projeto
./gradlew clean build -x test
```

#### Passo 2: Iniciar os serviços

```bash
cd composer
docker compose -f docker-compose.local.yml up --build -d
```

#### Verificar se está funcionando

```bash
# Verificar containers em execução
docker ps

# Ver logs da aplicação
docker compose -f docker-compose.local.yml logs -f api
```

#### Acessar a API

- **API Base:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/doc/tasklist/v1/api.html
- **Health Check:** http://localhost:8080/actuator/health

#### Parar os serviços

```bash
cd composer
docker compose -f docker-compose.local.yml down
```

### Opção 2: Execução Local (sem Docker)

#### Passo 1: Configurar o banco de dados PostgreSQL

Certifique-se de que o PostgreSQL está rodando localmente na porta 5432 com:
- **Database:** `tasklist_db`
- **User:** `postgres`
- **Password:** `postgres`

Ou ajuste as variáveis de ambiente no arquivo `src/main/resources/application.yml`.

#### Passo 2: Executar a aplicação

```bash
./gradlew bootRun
```

A aplicação estará disponível em: http://localhost:8080

## Como Rodar os Testes

Para executar todos os testes do projeto:

```bash
./gradlew test
```

Para executar os testes e gerar relatório de cobertura:

```bash
./gradlew test jacocoTestReport
```

Os relatórios estarão disponíveis em: `build/reports/jacoco/test/html/index.html`

**Nota:** Os testes utilizam H2 Database em memória, não sendo necessário ter PostgreSQL rodando para executá-los.

## Estrutura de Pastas

O projeto segue os princípios de **Clean Architecture**, organizando o código em camadas bem definidas:

```
src/main/java/br/com/jtech/tasklist/
├── adapters/                    # Camada de adaptadores (infraestrutura)
│   ├── input/                   # Adaptadores de entrada (controllers)
│   │   ├── controllers/         # Controllers REST
│   │   └── protocols/          # DTOs de requisição/resposta
│   └── output/                  # Adaptadores de saída (persistência)
│       ├── repositories/        # Repositórios JPA
│       │   ├── entities/        # Entidades JPA
│       │   └── *.java           # Interfaces de repositório
│       └── *.java               # Implementações de adapters
│
├── application/                 # Camada de aplicação (casos de uso)
│   ├── core/                    # Lógica de negócio central
│   │   ├── domains/             # Entidades de domínio
│   │   └── usecases/            # Casos de uso (regras de negócio)
│   └── ports/                   # Interfaces (contratos)
│       ├── input/               # Portas de entrada (use cases)
│       └── output/             # Portas de saída (gateways)
│
└── config/                      # Configurações da aplicação
    ├── infra/                   # Configurações de infraestrutura
    │   ├── exceptions/          # Tratamento de exceções
    │   ├── listeners/            # Event listeners
    │   ├── swagger/             # Configuração Swagger/OpenAPI
    │   └── utils/               # Utilitários
    └── usecases/                # Configuração de casos de uso (DI)

src/main/resources/
├── application.yml              # Configurações da aplicação
└── ...

composer/                        # Configurações Docker
├── docker-compose.local.yml     # Docker Compose para ambiente local
└── docker-compose.yml           # Docker Compose para produção
```

**Princípios da arquitetura:**
- **Domínio:** Entidades puras de negócio, sem dependências de frameworks
- **Aplicação:** Casos de uso que implementam as regras de negócio
- **Adaptadores:** Implementações concretas (controllers, repositórios)
- **Portas:** Interfaces que definem contratos entre camadas

## Decisões Técnicas

### 1. Clean Architecture

**Por quê?** A Clean Architecture foi escolhida para garantir:
- **Independência de frameworks:** O domínio não depende de Spring, JPA, etc.
- **Testabilidade:** Fácil criação de testes unitários sem necessidade de mocks complexos
- **Manutenibilidade:** Mudanças em frameworks não afetam a lógica de negócio
- **Escalabilidade:** Facilita adição de novas funcionalidades sem impactar código existente

### 2. PostgreSQL em vez de H2 para produção

**Por quê?**
- **H2** é utilizado apenas para testes (rápido e não requer configuração)
- **PostgreSQL** é usado em produção/desenvolvimento porque:
  - É um banco de dados robusto e amplamente utilizado
  - Suporta recursos avançados (transações, constraints, índices)
  - Melhor performance para aplicações em produção
  - Suporte a case-insensitive queries nativas (usado na validação de título único)

### 3. Spring Boot Actuator

**Por quê?** Implementado para:
- Monitoramento da saúde da aplicação (`/actuator/health`)
- Métricas e observabilidade
- Facilita troubleshooting em produção

### 4. Validação de Título Único Case-Insensitive

**Por quê?** Implementada usando query JPQL com `LOWER()`:
- Evita duplicatas acidentais (ex: "Teste" vs "teste")
- Melhora a experiência do usuário
- Garante integridade dos dados

### 5. Docker e Docker Compose

**Por quê?**
- Facilita setup do ambiente de desenvolvimento
- Garante consistência entre ambientes (dev, staging, prod)
- Isolamento de dependências (PostgreSQL, aplicação)
- Facilita deploy e CI/CD

### 6. SpringDoc OpenAPI (Swagger)

**Por quê?**
- Documentação automática e interativa da API
- Facilita testes durante desenvolvimento
- Reduz necessidade de documentação manual
- Padrão da indústria para APIs REST

### 7. Lombok

**Por quê?**
- Reduz boilerplate code (getters, setters, builders, etc.)
- Mantém código mais limpo e legível
- Reduz erros manuais em código repetitivo

## Melhorias Futuras

### Funcionalidades
- [ ] **Autenticação e Autorização:** Implementar JWT para segurança da API
- [ ] **Paginação:** Adicionar paginação nas listagens de tarefas
- [ ] **Filtros e Busca:** Permitir filtrar tarefas por status, data, etc.
- [ ] **Soft Delete:** Implementar exclusão lógica em vez de física
- [ ] **Histórico de Alterações:** Auditoria de mudanças nas tarefas
- [ ] **Categorias/Tags:** Permitir categorizar tarefas
- [ ] **Prioridades:** Adicionar níveis de prioridade às tarefas
- [ ] **Datas de Vencimento:** Permitir definir e validar prazos

### Técnicas
- [ ] **Cache:** Implementar cache para consultas frequentes (Redis)
- [ ] **Testes de Integração:** Adicionar testes end-to-end
- [ ] **CI/CD:** Pipeline automatizado de build e deploy
- [ ] **Logging Estruturado:** Implementar logging com formato JSON
- [ ] **Métricas:** Integração com Prometheus/Grafana
- [ ] **Rate Limiting:** Proteção contra abuso da API
- [ ] **Versionamento de API:** Suporte a múltiplas versões da API
- [ ] **Documentação:** Adicionar exemplos de requisições/respostas na documentação Swagger

### Performance
- [ ] **Índices no Banco:** Otimizar queries com índices apropriados
- [ ] **Connection Pooling:** Otimizar configuração do HikariCP
- [ ] **Async Processing:** Operações assíncronas para tarefas pesadas

### DevOps
- [ ] **Health Checks Avançados:** Verificação de dependências (DB, etc.)
- [ ] **Configuração Externa:** Usar Spring Cloud Config ou similar
- [ ] **Secrets Management:** Gerenciamento seguro de credenciais

---

**Desenvolvido para o Desafio Técnico Fullstack 1 - JTech**
