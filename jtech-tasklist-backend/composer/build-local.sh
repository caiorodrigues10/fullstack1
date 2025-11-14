#!/bin/bash
# Script para build local do JAR e depois construir a imagem Docker

echo "🔨 Construindo JAR localmente..."
cd "$(dirname "$0")/.."
./gradlew clean build -x test

if [ $? -eq 0 ]; then
    echo "✅ JAR construído com sucesso!"
    echo "🐳 Construindo imagem Docker..."
    cd composer
    docker compose -f docker-compose.local.yml build api
    docker compose -f docker-compose.local.yml up -d
    echo "✅ Ambiente iniciado!"
else
    echo "❌ Erro ao construir JAR"
    exit 1
fi

