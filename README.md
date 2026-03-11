# Patient Management Microservices

A comprehensive Spring Boot Cloud microservices application for managing patient data with authentication, billing, and analytics capabilities.

## Table of Contents

- [Project Overview](#project-overview)
- [Architecture](#architecture)
- [Services](#services)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [Building Services](#building-services)
- [Running with Docker Compose](#running-with-docker-compose)
- [API Documentation](#api-documentation)
- [Configuration](#configuration)
- [Security](#security)
- [Messaging](#messaging)
- [Monitoring](#monitoring)
- [Troubleshooting](#troubleshooting)

---

## Project Overview

This is an enterprise-grade microservices application built with Spring Boot 3 and Spring Cloud. It provides a complete patient management system with:

- **Patient Management** - CRUD operations for patient records
- **Authentication & Authorization** - JWT-based security with role management
- **API Gateway** - Centralized routing with request validation
- **Billing** - gRPC-based inter-service communication
- **Analytics** - Real-time event processing via Kafka and RabbitMQ
- **Distributed Tracing** - Zipkin integration for observability
- **Service Discovery** - Eureka for dynamic service registration
- **Configuration Management** - Spring Cloud Config Server

---

## Architecture

### Service Architecture

```
                                    ┌─────────────────────┐
                                    │    API Gateway      │
                                    │    (Port: 4004)     │
                                    └──────────┬──────────┘
                                               │
                    ┌──────────────────────────┼─────────────────────────┐
                    │                          │                         │
          ┌─────────▼─────────┐      ┌─────────▼─────────┐      ┌────────▼────────┐
          │   Auth Service    │      │  Patient Service  │      │ Billing Service │
          │    (Port: 4005)   │      │    (Port: 4000)   │      │   (Port: 4001)  │
          └─────────┬─────────┘      └─────────┬─────────┘      └────────┬────────┘
                    │                          │                         │
                    │             ┌────────────┼────────────┐            │
                    │             │            │            │            │
           ┌────────▼─────────┐   │    ┌───────▼──────┐     │    ┌───────▼───────────┐
           │   Auth Database  │   │    │    Kafka     │     │    │ gRPC Communication│
           │  (PostgreSQL)    │   │    └───────▲──────┘     │    └───────────────────┘
           └──────────────────┘   │            │            │
                                  │     ┌──────┴──────┐     │
                                  │     │             │     │
                         ┌────────▼─────▼─────┐ ┌─────▼─────┴─┐
                         │Analytics Service   │ │ RabbitMQ    │
                         │   (Port: 4003)     │ │             │
                         └────────────────────┘ └─────────────┘
```

### Infrastructure Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              Docker Network (app-network)                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐    │
│   │  PostgreSQL │  │  PostgreSQL │  │   Kafka     │  │     Zipkin      │    │
│   │  patient-db │  │   auth-db   │  │  (9092/9093)│  │    (9411)       │    │
│   │   :5432     │  │   :5432     │  │             │  │                 │    │
│   └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────┘    │
│                                                                             │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────┐    │
│   │  Eureka     │  │   Config    │  │  RabbitMQ   │  │   API Gateway   │    │
│   │ Service Reg │  │   Server    │  │ (5672/15672)│  │                 │    │
│   │   :8761     │  │   :8888     │  │             │  │                 │    │
│   └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────┘    │
│                                                                             │
│   ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                         │
│   │  Patient    │  │   Auth      │  │  Analytics  │                         │
│   │  Service    │  │   Service   │  │   Service   │                         │
│   │   :4000     │  │   :4005     │  │   :4003     │                         │
│   └─────────────┘  └─────────────┘  └─────────────┘                         │
│                                                                             │
│   ┌─────────────┐                                                           │
│   │  Billing    │                                                           │
│   │  Service    │                                                           │
│   │   :4001     │                                                           │
│   └─────────────┘                                                           │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## Services

### 1. Service Registry (service-reg)
- **Port**: 8761
- **Technology**: Spring Boot + Netflix Eureka Server
- **Purpose**: Service discovery and registration
- **URL**: http://localhost:8761

### 2. Config Server (config-server)
- **Port**: 8888
- **Technology**: Spring Cloud Config Server
- **Purpose**: Centralized configuration management
- **URL**: http://localhost:8888

### 3. API Gateway (api-gateway)
- **Port**: 4004
- **Technology**: Spring Cloud Gateway
- **Purpose**: Request routing, JWT validation, rate limiting
- **Features**:
  - JWT token validation
  - Route to auth-service (/auth/**)
  - Route to patient-service (/api/patients/**)
  - OpenAPI documentation proxy

### 4. Auth Service (auth-service)
- **Port**: 4005
- **Technology**: Spring Boot + Spring Security
- **Database**: PostgreSQL (auth_db)
- **Purpose**: User authentication and JWT token generation
- **Endpoints**:
  - POST /login - User login
  - POST /validate - Token validation

### 5. Patient Service (patient-service)
- **Port**: 4000
- **Technology**: Spring Boot + Spring Data JPA
- **Database**: PostgreSQL (patient_db)
- **Purpose**: Patient CRUD operations
- **Features**:
  - RESTful API with OpenAPI/Swagger
  - Circuit breaker (Resilience4j)
  - Kafka event publishing
  - RabbitMQ messaging
  - gRPC client for billing service
- **Endpoints**:
  - GET /patients - List all patients
  - GET /patients/{id} - Get patient by ID
  - POST /patients - Create new patient
  - PUT /patients/{id} - Update patient
  - DELETE /patients/{id} - Delete patient

### 6. Billing Service (billing-service)
- **Port**: 4001
- **Technology**: Spring Boot + gRPC
- **Purpose**: Billing account management
- **Communication**: gRPC
- **Features**:
  - gRPC server for billing operations
  - Protobuf message definitions

### 7. Analytics Service (analytics-service)
- **Port**: 4003
- **Technology**: Spring Boot + Kafka + RabbitMQ
- **Purpose**: Event processing and analytics
- **Features**:
  - Kafka consumer for patient events
  - RabbitMQ consumer for messages
  - gRPC client for billing data

---

## Technology Stack

### Core Framework
- **Spring Boot**: 3.3.3
- **Spring Cloud**: 2023.0.3 (Release Train)
- **Java**: 17

### Service Components
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: Netflix Eureka
- **Config Server**: Spring Cloud Config
- **Load Balancing**: Spring Cloud LoadBalancer

### Databases
- **PostgreSQL**: 15 (patient_db, auth_db)

### Messaging
- **Apache Kafka**: 4.1.1 (Event streaming)
- **RabbitMQ**: 3 (Message broker)

### Communication
- **REST**: Spring Web
- **gRPC**: grpc-netty-shaded, grpc-protobuf
- **OpenFeign**: Service-to-service HTTP calls

### Security
- **JWT**: JWT token-based authentication
- **Spring Security**: Authorization and authentication

### Resilience
- **Resilience4j**: Circuit breaker, retry, rate limiter

### Observability
- **Zipkin**: Distributed tracing
- **Micrometer**: Metrics collection
- **Spring Actuator**: Health checks and management

### Build Tools
- **Maven**: Build and dependency management
- **Docker**: Containerization

---

## Prerequisites

1. **Java Development Kit (JDK)**: Version 17 or higher
2. **Maven**: Version 3.8+
3. **Docker**: Latest version
4. **Docker Compose**: V2 or higher

---

## Getting Started

### 1. Clone the Repository

```bash
git clone <repository-url>
cd mark-patient-spring-boot-cloud
```

### 2. Build All Services

Build each microservice using Maven:

```bash
# Service Registry
cd service-reg && ./mvnw clean package -DskipTests

# Config Server
cd config-server && ./mvnw clean package -DskipTests

# API Gateway
cd api-gateway && ./mvnw clean package -DskipTests

# Auth Service
cd auth-service && ./mvnw clean package -DskipTests

# Patient Service
cd patient-service && ./mvnw clean package -DskipTests

# Billing Service
cd billing-service && ./mvnw clean package -DskipTests

# Analytics Service
cd analytics-service && ./mvnw clean package -DskipTests
```

Or use the provided Maven wrapper:

```bash
# From project root, build all services
for service in service-reg config-server api-gateway auth-service patient-service billing-service analytics-service; do
    cd $service && ../mvnw clean package -DskipTests && cd ..;
done
```

---

## Building Services

### Building Docker Images

#### Method 1: Using Spring Boot Build Image

```bash
# Build and push each service
cd service-reg
sudo ../mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=markcus0526/service-reg -DskipTests
docker push markcus0526/service-reg

cd ../config-server
sudo ../mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=markcus0526/config-server -DskipTests
docker push markcus0526/config-server

cd ../api-gateway
sudo ../mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=markcus0526/api-gateway -DskipTests
docker push markcus0526/api-gateway

cd ../auth-service
sudo ../mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=markcus0526/auth-service -DskipTests
docker push markcus0526/auth-service

cd ../patient-service
sudo ../mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=markcus0526/patient-service -DskipTests
docker push markcus0526/patient-service

cd ../billing-service
sudo ../mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=markcus0526/billing-service -DskipTests
docker push markcus0526/billing-service

cd ../analytics-service
sudo ../mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=markcus0526/analytics-service -DskipTests
docker push markcus0526/analytics-service
```

#### Method 2: Using Dockerfile

```bash
# Example for each service
cd service-reg
docker build -t markcus0526/service-reg:latest .
docker push markcus0526/service-reg:latest

# Repeat for other services...
```

---

## Running with Docker Compose

### Start All Services

```bash
# From project root
docker-compose up -d
```

### Check Service Health

```bash
# Check all containers
docker-compose ps

# View logs
docker-compose logs -f [service-name]

# Example: Check patient-service logs
docker-compose logs -f patient-service
```

### Service Ports

| Service          | Port  | URL                             |
|------------------|-------|---------------------------------|
| Service Registry | 8761  | http://localhost:8761           |
| Config Server    | 8888  | http://localhost:8888           |
| API Gateway      | 4004  | http://localhost:4004           |
| Auth Service     | 4005  | http://localhost:4005           |
| Patient Service  | 4000  | http://localhost:4000           |
| Billing Service  | 4001  | http://localhost:4001           |
| Analytics        | 4003  | http://localhost:4003           |
| Zipkin           | 9411  | http://localhost:9411           |
| RabbitMQ         | 5672  | amqp://localhost:5672           |
| RabbitMQ Admin   | 15672 | http://localhost:15672          |

### Stop All Services

```bash
docker-compose down
```

### Clean Up Volumes

```bash
docker-compose down -v
```

---

## API Documentation

### Authentication Flow

1. **Login**: POST /auth/login
```bash
curl -X POST http://localhost:4004/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@hospital.com", "password": "admin123"}'
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

2. **Use Token**: Include JWT in Authorization header
```bash
curl -X GET http://localhost:4004/api/patients \
  -H "Authorization: Bearer <your-token>"
```

### Patient Service APIs

#### Create Patient
```bash
curl -X POST http://localhost:4004/api/patients \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "address": "123 Main St",
    "dateOfBirth": "1990-01-15",
    "gender": "MALE"
  }'
```

#### Get All Patients
```bash
curl -X GET http://localhost:4004/api/patients \
  -H "Authorization: Bearer <token>"
```

#### Get Patient by ID
```bash
curl -X GET http://localhost:4004/api/patients/{id} \
  -H "Authorization: Bearer <token>"
```

#### Update Patient
```bash
curl -X PUT http://localhost:4004/api/patients/{id} \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Updated",
    "email": "john.updated@example.com"
  }'
```

#### Delete Patient
```bash
curl -X DELETE http://localhost:4004/api/patients/{id} \
  -H "Authorization: Bearer <token>"
```

### API Documentation URLs

- Patient Service: http://localhost:4004/api-docs/patients/v3/api-docs
- Auth Service: http://localhost:4004/api-docs/auth/v3/api-docs

---

## Configuration

### Environment Variables

Each service can be configured using environment variables:

```yaml
# Example configuration
spring:
  config:
    import: optional:configserver:http://config-server:8888
  profiles:
    active: docker

eureka:
  client:
    serviceUrl:
      defaultZone: http://service-reg:8761/eureka/

management:
  tracing:
    sampling:
      probability: 1.0
  zipkin:
    tracing:
      endpoint: http://zipkin:9411/api/v2/spans
```

### Application Properties

Key configuration files:
- `service-reg/src/main/resources/application.properties`
- `config-server/src/main/resources/application.properties`
- `api-gateway/src/main/resources/application.yml`
- `auth-service/src/main/resources/application.properties`
- `patient-service/src/main/resources/application.properties`
- `billing-service/src/main/resources/application.properties`
- `analytics-service/src/main/resources/application.properties`

### Externalized Configuration

The project uses Spring Cloud Config Server for centralized configuration. Configuration files are stored in `application-config/` directory.

---

## Security

### JWT Configuration

The JWT secret key is configured in the API Gateway:

```yaml
jwt:
  secret: "9a4f2c8d3b7a1e6f5d8c3a2b1e6f5d8c3a2b1e6f5d8c3a2b1e6f5d8c3a2b1e6f"
  expiration: 86400000  # 24 hours in milliseconds
```

**Important**: Change the JWT secret in production!

### Default Users

The auth-service initializes default users from `auth-service/src/main/resources/data.sql`:

- Email: `admin@hospital.com`
- Password: `admin123`

### Protected Routes

All `/api/patients/**` routes require valid JWT token. The `/auth/**` routes are public.

---

## Messaging

### Apache Kafka

Patient events are published to Kafka topics:
- **Topic**: `patient-events`
- **Message Format**: Protobuf (defined in `patient_event.proto`)

### RabbitMQ

- **Exchange**: patient-exchange (topic)
- **Queue**: patient-queue
- **Routing Key**: patient.#

### Event Flow

```
Patient Service ──publishes──> Kafka ──consumes──> Analytics Service
       │
       └──publishes──> RabbitMQ ──consumes──> Analytics Service
```

---

## Monitoring

### Health Checks

All services expose actuator health endpoints:

```bash
# Check service health
curl http://localhost:4000/actuator/health
```

### Zipkin Tracing

Access Zipkin UI at http://localhost:9411 to view distributed traces.

### Circuit Breaker Monitoring

```bash
# Patient service circuit breaker state
curl http://localhost:4000/actuator/health
```

Circuit breaker configuration (Resilience4j):
- Sliding window size: 10 calls
- Failure rate threshold: 50%
- Wait duration in open state: 10 seconds

---

## Troubleshooting

### Common Issues

#### 1. Services Not Registering with Eureka

**Solution**: Ensure Eureka server is running first:
```bash
docker-compose up -d service-reg
# Wait for Eureka to be healthy
docker-compose up -d
```

#### 2. Config Server Not Loading

**Solution**: Increase Git timeout:
```yaml
spring.cloud.config.server.git.timeout: 30
```

#### 3. Database Connection Issues

**Solution**: Check PostgreSQL containers:
```bash
docker-compose logs patient-db
docker-compose logs auth-db
```

#### 4. Kafka Connection Issues

**Solution**: Verify Kafka is running:
```bash
docker-compose logs kafka
```

#### 5. JWT Token Validation Failing

**Solution**: Ensure auth-service is running and accessible:
```bash
docker-compose logs auth-service
```

### Logs Viewing

```bash
# View all logs
docker-compose logs -f

# View specific service
docker-compose logs -f patient-service

# View last 100 lines
docker-compose logs --tail=100 patient-service
```

### Restart Services

```bash
# Restart specific service
docker-compose restart patient-service

# Rebuild and restart
docker-compose up -d --build patient-service
```

---

## Project Structure

```
mark-patient-spring-boot-cloud/
├── service-reg/               # Eureka Service Registry
│   ├── src/main/java/
│   └── pom.xml
├── config-server/             # Spring Cloud Config Server
│   ├── src/main/java/
│   └── pom.xml
├── api-gateway/               # API Gateway (Spring Cloud Gateway)
│   ├── src/main/java/
│   │   └── com/pm/apigateway/
│   │       ├── filter/        # JWT Validation Filter
│   │       ├── client/        # Feign Clients
│   │       └── exception/     # Exception Handling
│   └── pom.xml
├── auth-service/              # Authentication Service
│   ├── src/main/java/
│   │   └── com/pm/authservice/
│   │       ├── controller/    # REST Controllers
│   │       ├── service/       # Business Logic
│   │       ├── model/         # Entities
│   │       ├── dto/           # Data Transfer Objects
│   │       └── util/          # JWT Utilities
│   ├── src/main/resources/
│   │   └── data.sql           # Default Users
│   └── pom.xml
├── patient-service/           # Patient Management Service
│   ├── src/main/java/
│   │   └── com/pm/patientservice/
│   │       ├── controller/    # REST Controllers
│   │       ├── service/       # Business Logic
│   │       ├── model/         # Entities
│   │       ├── dto/           # Data Transfer Objects
│   │       ├── repository/    # Data Access
│   │       ├── mapper/        # Entity Mappers
│   │       ├── kafka/         # Kafka Producers
│   │       ├── rabbitmq/      # RabbitMQ Producers
│   │       ├── grpc/          # gRPC Clients
│   │       ├── config/        # Configuration
│   │       └── exception/     # Exception Handling
│   ├── src/main/proto/        # Protocol Buffers
│   ├── src/main/resources/
│   │   └── data.sql           # Sample Data
│   └── pom.xml
├── billing-service/           # Billing Service (gRPC Server)
│   ├── src/main/java/
│   │   └── com/pm/billingservice/
│   │       └── grpc/          # gRPC Implementation
│   ├── src/main/proto/
│   └── pom.xml
├── analytics-service/         # Analytics Service
│   ├── src/main/java/
│   │   └── com/pm/analyticsservice/
│   │       ├── kafka/         # Kafka Consumers
│   │       ├── rabbitmq/      # RabbitMQ Consumers
│   │       └── config/        # Configuration
│   ├── src/main/proto/
│   └── pom.xml
├── application-config/        # External Configurations
│   ├── *-service-docker.properties
│   └── *-service-docker.yml
├── api-requests/              # HTTP Request Examples
│   ├── auth-service/
│   └── patient-service/
├── grpc-requests/             # gRPC Request Examples
│   └── billing-service/
├── docker-compose.yml         # Docker Compose Configuration
├── docker-commands.txt        # Build Commands
└── README.md                  # This File
```

---

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

---

## License

This project is proprietary and confidential.

---

## Support

For issues and questions, please contact the development team.

