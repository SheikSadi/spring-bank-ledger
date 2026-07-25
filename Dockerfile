# Stage 1: Build Jar using Temurin JDK 25
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

# Copy Gradle wrapper and configuration files first (caches dependency downloads)
COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew
# Copy source code and build executable bootJar
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Stage 2: Minimal Runtime Image
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

# Create non-root user for security best practices
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy built JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# Default to mysql profile (can be overridden by SPRING_PROFILES_ACTIVE environment variable)
ENV SPRING_PROFILES_ACTIVE=mysql

# Run Spring Boot app
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
