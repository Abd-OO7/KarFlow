# ── Stage 1 : build ──────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copier le wrapper Maven et les métadonnées pom.xml d'abord (cache des dépendances)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copier les sources et compiler
COPY src src
RUN ./mvnw package -DskipTests -q

# ── Stage 2 : runtime ─────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Utilisateur non-root
RUN addgroup -S karflow && adduser -S karflow -G karflow

# Copier le JAR depuis le stage builder
COPY --from=builder /app/target/*.jar app.jar

# Répertoire pour les uploads
RUN mkdir -p /app/uploads && chown -R karflow:karflow /app

USER karflow

EXPOSE 8080

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
