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

# Create a non-root user for better security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy only the built jar
COPY --from=build /app/target/*.jar app.jar

# Standard Spring Boot environment variables
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]
