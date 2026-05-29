# ── Stage 1: Build ───────────────────────────────────────
FROM gradle:8.7-jdk21 AS builder
WORKDIR /app

# Cache Gradle dependencies separately from source code
COPY settings.gradle build.gradle ./
RUN gradle dependencies --no-daemon || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ── Stage 2: Runtime ─────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
