#!/bin/bash
# Script para build com BuildKit habilitado

export DOCKER_BUILDKIT=1
export COMPOSE_DOCKER_CLI_BUILD=1

cd "$(dirname "$0")"
docker compose build --progress=plain api
docker compose up -d

