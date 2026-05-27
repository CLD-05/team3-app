# ─────────────────────────────────────────────────
# Stage 1: Build
# ─────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 의존성 캐싱 레이어
COPY pom.xml .
RUN mvn dependency:go-offline -q

COPY src src
RUN mvn clean package -DskipTests -q

# ─────────────────────────────────────────────────
# Stage 2: Runtime
# ─────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

RUN apt-get update && \
    apt-get install -y --no-install-recommends curl && \
    rm -rf /var/lib/apt/lists/* && \
    groupadd --system foldy && \
    useradd  --system --gid foldy foldy

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar
RUN chown foldy:foldy app.jar

USER foldy

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]