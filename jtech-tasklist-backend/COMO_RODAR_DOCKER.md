# 🐳 Como Rodar o Projeto com Docker

Este guia explica como usar Docker para rodar o projeto, incluindo o banco de dados PostgreSQL.

## 🗄️ Opção 1: Rodar Apenas o PostgreSQL com Docker

Esta é a opção mais simples e recomendada. O PostgreSQL roda no Docker, mas a aplicação Java roda localmente.

### Passo 1: Iniciar o PostgreSQL

```bash
# Navegue até a pasta composer
cd composer

# Inicie o PostgreSQL em background
docker-compose up -d

# Ou para ver os logs em tempo real
docker-compose up
```

### Passo 2: Verificar se Está Rodando

```bash
# Verificar containers em execução
docker ps

# Você deve ver algo como:
# CONTAINER ID   IMAGE                STATUS         PORTS                    NAMES
# abc123def456   postgres:15-alpine    Up 2 minutes   0.0.0.0:5432->5432/tcp  jtech-postgres
```

### Passo 3: Rodar a Aplicação Java Localmente

Agora que o PostgreSQL está rodando, você pode rodar a aplicação Java normalmente:

```bash
# Volte para a raiz do projeto
cd ..

# Execute a aplicação
./gradlew bootRun

# Ou no Windows
.\gradlew.bat bootRun
```

### Configuração do Banco de Dados

O PostgreSQL estará disponível em:
- **Host:** `localhost`
- **Porta:** `5432`
- **Database:** `tasklist_db`
- **Usuário:** `postgres`
- **Senha:** `postgres`

Essas configurações já estão definidas no `application.yml` e não precisam ser alteradas.

---

## 🛑 Comandos Úteis para o PostgreSQL

### Parar o PostgreSQL

```bash
cd composer
docker-compose down
```

### Parar e Remover os Dados (Limpar Tudo)

```bash
cd composer
docker-compose down -v
```

⚠️ **Atenção:** O comando `-v` remove os volumes, apagando todos os dados do banco!

### Ver Logs do PostgreSQL

```bash
cd composer
docker-compose logs -f postgres
```

### Reiniciar o PostgreSQL

```bash
cd composer
docker-compose restart
```

### Verificar Status

```bash
cd composer
docker-compose ps
```

### Conectar ao Banco via Terminal

```bash
# Entrar no container do PostgreSQL
docker exec -it jtech-postgres psql -U postgres -d tasklist_db

# Ou executar um comando SQL direto
docker exec -it jtech-postgres psql -U postgres -d tasklist_db -c "SELECT * FROM tasks;"
```

---

## 📊 Verificar Dados no Banco

### Listar Tabelas

```bash
docker exec -it jtech-postgres psql -U postgres -d tasklist_db -c "\dt"
```

### Ver Dados da Tabela Tasks

```bash
docker exec -it jtech-postgres psql -U postgres -d tasklist_db -c "SELECT * FROM tasks;"
```

### Contar Registros

```bash
docker exec -it jtech-postgres psql -U postgres -d tasklist_db -c "SELECT COUNT(*) FROM tasks;"
```

## 🎯 Fluxo Completo de Desenvolvimento

### 1. Iniciar o Ambiente

```bash
# Terminal 1: Iniciar PostgreSQL
cd composer
docker-compose up -d

# Terminal 2: Rodar a aplicação
cd ..
./gradlew bootRun
```

### 2. Desenvolvimento

- A aplicação estará em: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/doc/tasklist/v1/api.html`
- PostgreSQL: `localhost:5432`

---

## 📚 Acessar a Documentação Swagger

Após iniciar a aplicação, você pode acessar a documentação interativa da API:

### URL do Swagger UI

```
http://localhost:8080/doc/tasklist/v1/api.html
```

### O que você pode fazer no Swagger:

- ✅ Ver todos os endpoints disponíveis
- ✅ Testar os endpoints diretamente no navegador
- ✅ Ver exemplos de requisições e respostas
- ✅ Verificar os modelos de dados (schemas)
- ✅ Testar autenticação (quando implementada)

### Endpoints Documentados:

- `POST /tasks` - Criar nova tarefa
- `GET /tasks` - Listar todas as tarefas
- `GET /tasks/{id}` - Buscar tarefa por ID
- `PUT /tasks/{id}` - Atualizar tarefa
- `DELETE /tasks/{id}` - Deletar tarefa
- `POST /api/v1/tasklists` - Criar tasklist

### Documentação OpenAPI (JSON)

A documentação OpenAPI também está disponível em formato JSON:

```
http://localhost:8080/doc/tasklist/v3/api-documents
```

Este endpoint retorna a especificação OpenAPI 3.0 em formato JSON, útil para integração com ferramentas externas.

### 3. Parar o Ambiente

```bash
# Parar a aplicação: Ctrl + C no terminal

# Parar o PostgreSQL
cd composer
docker-compose down
```

---

## 🔧 Solução de Problemas

### Erro: "Port 5432 is already in use"

Isso significa que a porta 5432 já está sendo usada por outro processo.

**Solução 1: Parar o processo que está usando a porta**

**Windows:**
```bash
# Encontrar o processo
netstat -ano | findstr :5432

# Matar o processo (substitua PID pelo número encontrado)
taskkill /PID <PID> /F
```

**Mac/Linux:**
```bash
# Encontrar o processo
lsof -i :5432

# Matar o processo (substitua PID pelo número encontrado)
kill -9 <PID>
```

**Solução 2: Alterar a Porta do Docker**

Edite o arquivo `composer/docker-compose.yml`:

```yaml
services:
  postgres:
    ports:
      - "5433:5432"  # Mude 5432 para 5433 (ou outra porta)
```

E atualize o `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5433/tasklist_db
```

### Erro: "Cannot connect to database"

**Verifique:**
1. O container está rodando: `docker ps`
2. O PostgreSQL está saudável: `docker-compose ps` (deve mostrar "healthy")
3. A porta está correta no `application.yml`

**Reinicie o PostgreSQL:**
```bash
cd composer
docker-compose restart
```

### Erro: "Container name already exists"

Isso significa que já existe um container com o mesmo nome.

**Solução:**
```bash
# Remover o container antigo
docker rm -f jtech-postgres

# Ou parar e remover via docker-compose
cd composer
docker-compose down
docker-compose up -d
```

### Limpar Tudo e Começar do Zero

Se algo der errado e você quiser começar do zero:

```bash
cd composer

# Parar e remover tudo
docker-compose down -v

# Remover a imagem (opcional)
docker rmi postgres:15-alpine

# Iniciar novamente
docker-compose up -d
```

---

## 💾 Backup e Restore

### Fazer Backup do Banco

```bash
docker exec -t jtech-postgres pg_dump -U postgres tasklist_db > backup.sql
```

### Restaurar Backup

```bash
# Primeiro, certifique-se que o container está rodando
cd composer
docker-compose up -d

# Restaurar o backup
docker exec -i jtech-postgres psql -U postgres -d tasklist_db < backup.sql
```

---

## 📝 Resumo Rápido

```bash
# Iniciar PostgreSQL
cd composer && docker-compose up -d

# Verificar status
docker ps

# Ver logs
cd composer
docker-compose logs -f postgres

# Parar
cd composer
docker-compose down

# Parar e limpar dados
cd composer
docker-compose down -v
```

---

## ✅ Checklist

- [ ] Docker instalado e funcionando
- [ ] Docker Compose instalado
- [ ] PostgreSQL rodando (`docker ps`)
- [ ] Aplicação Java conectando ao banco
- [ ] Swagger acessível em `http://localhost:8080/doc/tasklist/v1/api.html`

---

**Pronto! Agora você sabe como usar Docker no projeto!** 🚀

