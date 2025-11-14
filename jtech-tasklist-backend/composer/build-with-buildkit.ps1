# PowerShell script para build com BuildKit
$env:DOCKER_BUILDKIT=1
$env:COMPOSE_DOCKER_CLI_BUILD=1

docker compose build --progress=plain api
docker compose up -d

