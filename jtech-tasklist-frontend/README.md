# JTech Tasklist (Frontend)

Aplicação Vue 3 (Vite + TypeScript + Pinia) que consome a API REST de gerenciamento de tarefas.

## Stack
- Vue 3 + Vite + TypeScript
- Pinia (estado)

## Configuração da API
- Base URL padrão: `http://localhost:8080`
- Opcional: defina `VITE_API_BASE_URL` para apontar para outro backend.
  - Exemplo (Windows PowerShell):
    ```ps1
    setx VITE_API_BASE_URL "http://localhost:8080"
    ```
  - Em Bash (Git Bash):
    ```bash
    export VITE_API_BASE_URL="http://localhost:8080"
    ```

## Como rodar (somente tela de Tasks)
```sh
npm install
npm run dev
```

## Build
```sh
npm run build
```

## Testes unitários
```sh
npm run test:unit
```

## Lint
```sh
npm run lint
```

## Docker

Build e run (porta 8080 -> 80 no container):
```sh
docker build -t jtech-tasklist-frontend .
docker run --rm -p 8080:80 jtech-tasklist-frontend
```

Definir a API em build-time (Vite lê env no build):
```sh
docker build --build-arg VITE_API_BASE_URL=http://host.docker.internal:8080 -t jtech-tasklist-frontend .
docker run --rm -p 8080:80 jtech-tasklist-frontend
```

Observações:
- Em Windows/Mac, `host.docker.internal` aponta para a máquina host (back-end local).
- Em Linux, use o IP da máquina host ou rede Docker apropriada.

## Rotas do Backend (referência para a tela)
Base URL: `http://localhost:8080`

- POST `/tasks` — Criar tarefa
  Body JSON:
  ```json
  { "title": "Minha Tarefa", "description": "Descrição", "status": "pendente" }
  ```
- GET `/tasks` — Listar todas as tarefas
- GET `/tasks/{id}` — Buscar tarefa por ID
- PUT `/tasks/{id}` — Atualizar tarefa
- DELETE `/tasks/{id}` — Deletar tarefa
- POST `/api/v1/tasklists` — Criar tasklist

Documentação:
- GET `/doc/tasklist/v1/api.html` — Swagger UI
- GET `/doc/tasklist/v3/api-documents` — OpenAPI JSON
