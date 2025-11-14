# Dockerfile otimizado para Windows - Aplicação Java
# syntax=docker/dockerfile:1.4

# Stage 1: Build
FROM gradle:8.5-jdk21-alpine AS build

WORKDIR /app

# Copiar todos os arquivos necessários de uma vez
# Evita múltiplas operações COPY que podem causar problemas de permissão no Windows
COPY build.gradle settings.gradle gradle.properties ./
COPY src ./src

# Build da aplicação (usando gradle da imagem, não wrapper)
RUN gradle clean build -x test --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Instalar wget para healthcheck
RUN apk add --no-cache wget

# Criar usuário não-root
RUN addgroup -S spring && adduser -S spring -G spring

# Copiar JAR do stage de build
COPY --from=build --chown=spring:spring /app/build/libs/*.jar app.jar

# Mudar para usuário não-root
USER spring:spring

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Executar aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]

