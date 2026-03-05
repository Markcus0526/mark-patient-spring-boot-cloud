# Stage 1: Build stage
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app

# Cache dependencies first (makes builds faster)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 1. Install curl while still root
RUN apk add --no-cache curl

# 2. Create a non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# 3. Copy the jar and change ownership
COPY --from=build --chown=spring:spring /app/target/*.jar app.jar

# 4. Switch to non-root user
USER spring:spring

# Standard Spring Boot environment variables
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]
