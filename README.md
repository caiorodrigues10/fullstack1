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

### Backend (`jtech-tasklist-backend`)

```
src/main/java/br/com/jtech/tasklist/
├── adapters/
│   ├── input/
│   │   ├── controllers/           # Controllers REST (TaskController, CreateTasklistController)
│   │   └── protocols/             # DTOs (TaskRequest/Response, TasklistRequest/Response)
│   └── output/
│       ├── repositories/          # JPA repositories + entities (Task, Tasklist)
│       └── *.java                 # Adapters de saída (TaskAdapter, CreateTasklistAdapter)
├── application/
│   ├── core/
│   │   ├── domains/               # Entidades de domínio (Task, Tasklist)
│   │   └── usecases/              # Casos de uso (TaskUseCase, CreateTasklistUseCase)
│   └── ports/
│       ├── input/                 # Portas de entrada (contratos de use cases)
│       └── output/                # Portas de saída (contratos de gateways)
├── config/
│   ├── infra/
│   │   ├── exceptions/            # Modelos/estruturas de erro e validação
│   │   ├── listeners/             # Eventos (ReadyEventListener)
│   │   ├── swagger/               # Configuração OpenAPI/Swagger
│   │   └── utils/                 # Utilitários (GlobalExceptionHandler, Jsons, GenId)
│   └── usecases/                  # Configuração dos casos de uso (beans)
└── StartTasklist.java             # Classe principal (Spring Boot)
```

Principais “páginas” (endpoints) expostas pelos controllers:
- `POST /tasks`, `GET /tasks`, `GET /tasks/{id}`, `PUT /tasks/{id}`, `DELETE /tasks/{id}`
- Documentação: `GET /doc/tasklist/v1/api.html` (Swagger UI)

Detalhes adicionais no `jtech-tasklist-backend/README.md`.

### Frontend (`jtech-tasklist-frontend`)

```
src/
├── App.vue                        # Shell da aplicação
├── main.ts                        # Bootstrap (Vite + Vue)
├── views/
│   └── TasksView.vue              # Página principal de tarefas
├── components/                    # Componentes de UI de apoio
│   ├── icons/                     # Ícones (SVG em componentes Vue)
│   └── ...                        # Demais componentes utilitários
├── stores/
│   └── tasks.ts                   # Store Pinia para gerenciar tarefas
├── services/
│   ├── http.ts                    # Cliente HTTP (fetch/axios wrapper)
│   └── tasks.ts                   # Serviço de integração com a API de tarefas
├── types/
│   └── task.ts                    # Tipos/Interfaces de domínio do frontend
├── composables/
│   └── useToast.ts                # Composable de toasts/notificações
└── assets/                        # Estilos e assets estáticos
```

Principais páginas/fluxos:
- `TasksView.vue`: lista, cria, atualiza e remove tarefas via API (`/tasks`).

Detalhes adicionais no `jtech-tasklist-frontend/README.md`.

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

Em caso de dúvidas, verifique primeiro os `README.md` dentro de `jtech-tasklist-backend` e `jtech-tasklist-frontend`, onde há instruções detalhadas e específicas de cada projeto.

