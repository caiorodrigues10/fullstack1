# 🐳 Como Rodar com Docker - Solução para Windows

## ⚠️ Problema de Permissões no Windows

Se você está tendo o erro `archive/tar: unknown file mode ?rwxr-xr-x`, use a **Solução 2** abaixo.

## ✅ Solução 1: Build Completo no Docker (se funcionar)

```bash
cd composer
docker compose up --build
```

## ✅ Solução 2: Build Local + Docker (Recomendado para Windows)

Esta solução constrói o JAR localmente e depois apenas copia para o container, evitando problemas de permissão.

### Passo 1: Construir o JAR localmente

```bash
# Na raiz do projeto
./gradlew clean build -x test
```

### Passo 2: Construir e iniciar com Docker

```bash
cd composer
docker compose -f docker-compose.local.yml up --build -d
```

### Ou use o script automatizado:

```bash
cd composer
bash build-local.sh
```

## 📋 Verificar se está funcionando

```bash
# Ver containers
docker ps

# Ver logs da API
docker compose logs -f api

# Ver logs do PostgreSQL
docker compose logs -f postgres
```

## 🛑 Parar os serviços

```bash
cd composer
docker compose down
```

## 🔄 Atualizar após mudanças no código

1. Reconstruir o JAR: `./gradlew clean build -x test`
2. Reconstruir a imagem: `docker compose -f docker-compose.local.yml build api`
3. Reiniciar: `docker compose -f docker-compose.local.yml up -d`

