# JTech Tasklist — Guia do Avaliador (Frontend + Backend)

## Visão Geral do Projeto

Este repositório contém dois projetos independentes e integrados:

- `jtech-tasklist-backend`: API RESTful em Java (Spring Boot) para gerenciamento de tarefas.
- `jtech-tasklist-frontend`: Aplicação web em Vue 3 (Vite + TypeScript) que consome a API.

Fluxo recomendado de avaliação:
1) Iniciar o backend (preferencialmente via Docker Compose).
2) Rodar o frontend em modo desenvolvimento (Vite).
3) Verificar a tela de tarefas consumindo a API em `http://localhost:8080` (valor padrão).

Cada subprojeto possui seu próprio `README.md` com detalhes. Este documento é um guia central para navegação e execução rápida.

---

## Stack Utilizada

### Backend
- Java 21, Spring Boot 3.5.5
- Spring Data JPA + Hibernate Validator
- PostgreSQL (desenvolvimento/produção) e H2 (testes)
- Gradle, SpringDoc OpenAPI (Swagger), Actuator, Lombok

### Frontend
- Vue 3 + Vite 7 + TypeScript 5
- Pinia (estado), Vitest (testes), ESLint + Prettier
- Node.js >= 20.19 (ou >= 22.12)

---

## Como Rodar Localmente

### Pré-requisitos
- Backend (opção 1 — recomendado): Docker e Docker Compose.
- Backend (opção 2): Java 21, PostgreSQL local configurado (ver `jtech-tasklist-backend/README.md`).
- Frontend: Node.js 20 LTS (ou mais recente suportado) e npm.

### 1) Backend

Opção A — Docker Compose (recomendado):

```bash
cd jtech-tasklist-backend/composer
docker compose -f docker-compose.local.yml up --build -d
```

Opção B — Execução local (sem Docker) — veja detalhes completos no `README.md` do backend:

```bash
cd jtech-tasklist-backend
# Linux/Mac (Git Bash)
./gradlew bootRun
# Windows (Prompt/PowerShell)
.\gradlew.bat bootRun
```

Após subir, acesse:
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/doc/tasklist/v1/api.html`

### 2) Frontend

```bash
cd jtech-tasklist-frontend
npm install
npm run dev
```

Aplicação (Vite): `http://localhost:5173`

Por padrão, o frontend chama a API em `http://localhost:8080`. Para apontar para outra URL, defina a variável `VITE_API_BASE_URL` antes do build/dev, por exemplo:

```bash
# Bash (Linux/Mac/Git Bash no Windows)
export VITE_API_BASE_URL="http://localhost:8080"

# PowerShell (Windows)
setx VITE_API_BASE_URL "http://localhost:8080"
```

Observação (Docker do frontend): se desejar rodar o frontend em Docker, altere a porta do host para evitar conflito com a API (ex.: `-p 8081:80`). Veja instruções no `jtech-tasklist-frontend/README.md`.

---

## Como Rodar os Testes

### Backend
```bash
cd jtech-tasklist-backend
# Linux/Mac (Git Bash)
./gradlew test
# Windows (Prompt/PowerShell)
.\gradlew.bat test
```
Relatório de cobertura (opcional): `./gradlew test jacocoTestReport`
Detalhes adicionais no `README.md` do backend.

### Frontend
```bash
cd jtech-tasklist-frontend
npm run test:unit
```

---

## Estrutura de Pastas

```
.
├── jtech-tasklist-backend/       # API Java (Spring Boot)
│   └── README.md                 # Documentação detalhada do backend
└── jtech-tasklist-frontend/      # Frontend Vue 3 + Vite + TypeScript
    └── README.md                 # Documentação detalhada do frontend
```

As estruturas internas (camadas, módulos, scripts) estão descritas em cada `README.md` específico.

---

## Decisões Técnicas

- **Separação por projetos**: Repositório com backend e frontend isolados, facilitando builds e deploys independentes.
- **Backend com Clean Architecture**: Domínio desacoplado de frameworks; testes com H2; documentação via OpenAPI/Swagger e endpoints de health com Actuator.
- **Bancos de dados**: PostgreSQL para ambiente real; H2 em memória para testes rápidos e reprodutíveis.
- **Containerização**: Docker Compose no backend para subir app + dependências rapidamente.
- **Frontend moderno**: Vue 3 com Vite (desenvolvimento ágil e build rápido), estado com Pinia, testes com Vitest.
- **Configuração do cliente**: `VITE_API_BASE_URL` para apontar para diferentes ambientes da API sem alterar código.

Para racional completo e detalhes de implementação, consulte os `README.md` de cada subprojeto.

---

## Melhorias Futuras

- Unificar execução local com um `docker-compose` de topo (frontend + backend + DB).
- Adicionar testes end-to-end (Cypress/Playwright) cobrindo o fluxo de tarefas.
- Pipeline de CI/CD (build, testes, verificação de qualidade e publicação de artefatos).
- Observabilidade: métricas/dashboards (Prometheus/Grafana) e logs estruturados.
- Segurança: autenticação/autorização (JWT) e rate limiting na API.
- Versionamento de API e contratos tipados (OpenAPI → client types para o frontend).
- Migrações de banco (Flyway) e dados seed para revisão mais rápida.

---

Em caso de dúvidas, verifique primeiro os `README.md` dentro de `jtech-tasklist-backend` e `jtech-tasklist-frontend`, onde há instruções detalhadas e específicas de cada projeto.
# Desafio Técnico Fullstack 1 - JTech

## API RESTful para Gerenciamento de Tarefas

### Contextualização e Objetivo

A **JTech** busca identificar profissionais que demonstrem sólido conhecimento nos fundamentos do desenvolvimento backend. Este desafio técnico foi elaborado para avaliar suas competências na construção de APIs RESTful utilizando Java e Spring Boot.

**Objetivo:** Desenvolver uma API completa para gerenciamento de tarefas (TODO List), aplicando boas práticas de desenvolvimento, arquitetura limpa e documentação técnica de qualidade.

## Especificações Técnicas

### Requisitos Funcionais

1. **Criar Tarefa**: Endpoint `POST /tasks` para adicionar uma nova tarefa. A tarefa deve conter título, descrição e status (ex: "pendente", "concluída").
2. **Listar Tarefas**: Endpoint `GET /tasks` para retornar todas as tarefas cadastradas.
3. **Buscar Tarefa por ID**: Endpoint `GET /tasks/{id}` para obter os detalhes de uma tarefa específica.
4. **Atualizar Tarefa**: Endpoint `PUT /tasks/{id}` para atualizar o título, a descrição ou o status de uma tarefa.
5. **Deletar Tarefa**: Endpoint `DELETE /tasks/{id}` para remover uma tarefa do sistema.

### Requisitos Não Funcionais

1. **Persistência de Dados**: As tarefas devem ser armazenadas em banco de dados. Recomenda-se H2 (em memória) para simplificação ou PostgreSQL para demonstrar conhecimento em bancos relacionais.
2. **Validação de Dados**: Implementar validação robusta das entradas do usuário (ex: título da tarefa obrigatório e não vazio).
3. **Tratamento de Erros**: A API deve retornar códigos de status HTTP apropriados e mensagens de erro claras (ex: 404 para tarefa não encontrada, 400 para dados inválidos).

### Stack Tecnológica Obrigatória

* **Linguagem**: Java
* **Framework**: Spring Boot
* **Persistência**: Spring Data JPA com Hibernate
* **Banco de Dados**: H2 (em memória) ou PostgreSQL
* **Testes**: Testes unitários com JUnit/Mockito.

## Critérios de Avaliação

* **Qualidade e Organização do Código**: Código limpo, legível e seguindo as convenções do Java.
* **Aplicação de Boas Práticas**: Utilização de princípios como Clean Code e KISS.
* **Funcionalidade**: Todos os endpoints devem funcionar conforme especificado.
* **Testes Automatizados**: Cobertura de testes unitários para as classes de serviço e controllers.
* **Uso Adequado da Stack**: Configuração correta do Spring Boot, JPA e do banco de dados.
* **Modelagem de Dados**: Estrutura da entidade `Task` bem definida.
* **Controle de Versão**: Commits claros e lógicos no Git.

## Expectativa de Entrega

* **Prazo**: Até 3 dias corridos a partir do recebimento.
* **Formato**: Entregar o código-fonte em um repositório Git, acompanhado de um `README.md` completo.

### Estrutura Obrigatória do `README.md`

1. **Visão Geral do Projeto**: Breve descrição da API e seus objetivos.
2. **Stack Utilizada**: Lista das tecnologias implementadas.
3. **Como Rodar Localmente**: Instruções para configurar o ambiente, instalar dependências e iniciar o servidor.
4. **Como Rodar os Testes**: Comando para executar os testes.
5. **Estrutura de Pastas**: Explicação da organização do projeto.
6. **Decisões Técnicas**: Justificativas para as escolhas feitas (ex: por que usou H2 em vez de PostgreSQL).
7. **Melhorias Futuras**: Sugestões para evoluir a API.

---

**Boa sorte! A JTech está ansiosa para conhecer sua solução.**
