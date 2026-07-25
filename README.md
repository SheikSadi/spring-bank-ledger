# Core Banking Ledger Microservice (`ledger-api`)

[![Java 25](https://img.shields.io/badge/Java-25-orange.svg)](https://jdk.java.net/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-9.6.1-02303A.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A high-reliability, production-grade microservice implementing a financial account ledger. Built with **Java 25** and **Spring Boot 4.1.0**, this service demonstrates essential patterns required for modern core banking platforms, including high-concurrency pessimistic locking, payment-grade HTTP idempotency key replay, Flyway database migrations, OAuth2 JWT stateless security, and structured ECS JSON logging.

---

## 🏛️ Architectural Highlights & Key Features

* **Double-Entry Financial Integrity & Precision:**
  * Uses `BigDecimal` for arbitrary-precision monetary calculations (never floating-point `double`).
  * Enforces strict business validations on credit and debit requests (preventing negative amounts and insufficient balance states).
* **Database-Level Concurrency Control (Pessimistic Write Locks):**
  * Implements `@Transactional` write paths backed by JPA pessimistic write locks (`findWithWriteLock` → `SELECT ... FOR UPDATE`) to guarantee zero race conditions during simultaneous debits/credits on high-throughput account balances.
* **Payment-Grade Idempotency Filter (`Idempotency-Key`):**
  * Custom HTTP servlet filter (`OncePerRequestFilter`) inspecting the `Idempotency-Key` header.
  * Caches successful 2xx responses (`ContentCachingResponseWrapper`) and replays identical status codes, headers, and payload bodies on duplicate/retry requests without re-executing business logic or modifying balances.
* **Flyway Versioned Schema Migrations:**
  * Schema versioning owned by Flyway migrations (`V1__create_accounts_table.sql`, `V2__create_idempotency_keys.sql`) enforcing `ddl-auto=none` in production profiles.
* **Stateless OAuth2 JWT Security:**
  * Endpoint protection using Spring Security Resource Server with Nimbus JWT decoding.
  * Dedicated authentication controller (`POST /auth/login`) issuing signed JWT tokens.
* **Multi-Profile Repository Segregation:**
  * Modular data access layer supporting seamless profile switching (`in-memory`, `dev`, `mysql`, `redis`) via Spring `@Profile` annotations and type-safe `@ConfigurationProperties`.
* **Observability & Error Model:**
  * Centralized global exception handler (`@RestControllerAdvice`) delivering clean `ErrorResponse` and `FieldErrorDetail` envelopes.
  * Native ECS structured JSON logging with thread-safe SLF4J `MDC` correlation ID propagation (`X-Correlation-Id`).
  * Spring Boot Actuator with custom health indicators (`LedgerConfigHealthIndicator`) and Kubernetes liveness/readiness probes.

---

## 🛠️ Tech Stack & Dependencies

| Category | Technology |
| :--- | :--- |
| **Language** | Java 25 (Temurin LTS) |
| **Framework** | Spring Boot 4.1.0 (Web MVC, Security, Data JPA, Actuator) |
| **Build Tool** | Gradle 9.6.1 (Kotlin DSL `build.gradle.kts`) |
| **Database & ORM** | MySQL 8.0, Spring Data JPA, Flyway 11, Redis |
| **Infrastructure (IaC)** | Terraform (AWS Tokyo `ap-northeast-1`: App Runner, ECR, RDS MySQL) |
| **Security** | Spring Security OAuth2 Resource Server (Nimbus JWT) |
| **Testing** | JUnit 5, AssertJ, Mockito, Spring Security Test, Spring Boot WebMvc Test |
| **API Docs** | OpenAPI 3.0 / Swagger UI (`springdoc-openapi`) |

---

## ☁️ Infrastructure as Code (AWS Tokyo Deployment with Terraform)

The repository includes a complete **Terraform** configuration under `terraform/` to provision production-grade AWS infrastructure in **Tokyo (`ap-northeast-1`)**:

### AWS Resources Provisioned
* **AWS ECR Repository:** `450963614191.dkr.ecr.ap-northeast-1.amazonaws.com/spring-bank-ledger`
* **AWS RDS MySQL 8.0:** `spring-bank-ledger-db.cn200ami8jvv.ap-northeast-1.rds.amazonaws.com:3306` (Free-Tier `db.t3.micro` with 20 GB `gp3` storage and automated Flyway migrations)
* **AWS App Runner:** Managed serverless container runtime (1 vCPU, 2GB RAM) running Spring Boot with automated HTTPS provisioning.

### Provisioning Steps
```bash
cd terraform

# 0. Export active AWS CLI session credentials to shell environment
eval $(aws configure export-credentials --format env)

# 1. Create S3 Bucket for Remote State (one-time setup)
aws s3api create-bucket \
  --bucket spring-bank-ledger-tfstate-sheiksadi \
  --region ap-northeast-1 \
  --create-bucket-configuration LocationConstraint=ap-northeast-1

# 2. Initialize Terraform (connects to AWS S3 remote backend)
terraform init

# 3. Provision ECR & RDS Database
terraform apply -target=aws_ecr_repository.app_repo -target=aws_db_instance.ledger_db

# 4. Authenticate Docker with AWS ECR Tokyo & Push Image
aws ecr get-login-password --region ap-northeast-1 | docker login --username AWS --password-stdin 450963614191.dkr.ecr.ap-northeast-1.amazonaws.com
docker build -t spring-bank-ledger .
docker tag spring-bank-ledger:latest 450963614191.dkr.ecr.ap-northeast-1.amazonaws.com/spring-bank-ledger:latest
docker push 450963614191.dkr.ecr.ap-northeast-1.amazonaws.com/spring-bank-ledger:latest

# 5. Provision App Runner Service
terraform apply
```

---

## 📁 Project Structure

```
ledger-api/
├── build.gradle.kts                   # Gradle Kotlin DSL build script
├── compose.yml                        # Docker Compose setup for MySQL 8.0
├── src/
│   ├── main/
│   │   ├── java/com/paypay/learn/ledger/
│   │   │   ├── Account.java           # Domain Record (Immutable value carrier)
│   │   │   ├── AccountEntity.java     # JPA Entity for MySQL persistence
│   │   │   ├── AccountController.java # REST endpoints for Account CRUD & Transactions
│   │   │   ├── AccountService.java    # Core domain business logic & transaction boundaries
│   │   │   ├── AccountRepository.java # Abstract repository interface
│   │   │   ├── JpaAccountRepository.java # MySQL JPA backed repository (Pessimistic locking)
│   │   │   ├── AuthController.java    # Authentication & JWT issuance
│   │   │   ├── SecurityConfig.java    # Spring Security Resource Server configuration
│   │   │   ├── IdempotencyFilter.java # Servlet filter intercepting Idempotency-Key
│   │   │   ├── CorrelationIdFilter.java # SLF4J MDC trace filter (X-Correlation-Id)
│   │   │   └── GlobalExceptionHandler.java # @RestControllerAdvice error mapper
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-mysql.properties
│   │       ├── application-redis.properties
│   │       └── db/migration/
│   │           ├── V1__create_accounts_table.sql
│   │           └── V2__create_idempotency_keys.sql
│   └── test/
│       └── java/com/paypay/learn/ledger/
│           ├── AccountServiceTest.java      # Unit tests with Mockito
│           ├── AccountControllerTest.java   # Slice tests (@WebMvcTest)
│           ├── AccountIntegrationTest.java  # E2E & Concurrency tests (@SpringBootTest)
│           └── AuthIntegrationTest.java     # Security JWT integration tests
```

---

## 🚀 Getting Started

### Prerequisites
* **Java 25** (or compatible JDK)
* **Docker & Docker Compose** (for running MySQL 8.0)
* **Gradle 9.x** (or use the bundled `./gradlew`)

### 1. Spin up Infrastructure (MySQL)
From the root repository directory:
```bash
docker compose up -d
```

### 2. Build & Run the Application
Run locally with the default `in-memory` profile:
```bash
./gradlew bootRun
```

Run with the `mysql` profile (connects to Docker MySQL & executes Flyway migrations):
```bash
SPRING_PROFILES_ACTIVE=mysql ./gradlew bootRun
```

### 3. Interactive API Documentation (Swagger UI)
Once running, open your browser to interact with the API:
* **Swagger UI:** `http://localhost:8080/swagger-ui/index.html`
* **OpenAPI Spec:** `http://localhost:8080/v3/api-docs`

---

## 🧪 Testing

The repository features comprehensive unit, slice, and integration test suites:

```bash
# Run all automated tests
./gradlew test
```

### Highlighted Tests
* **`AccountServiceTest.ConcurrencyTests`:** Verifies pessimistic locking (`SELECT ... FOR UPDATE`) under multi-threaded parallel debit requests to guarantee zero balance corruption.
* **`AccountIntegrationTest.IdempotencyTests`:** Asserts single-execution and identical response body replay when retrying requests with matching `Idempotency-Key` headers.
* **`AuthIntegrationTest`:** Tests JWT issuance, valid bearer token access, and 401 Unauthorized rejection on secured endpoints.

---

## 📡 API Endpoints Reference

### Public Endpoints
* `POST /auth/login` — Authenticate and receive a Bearer JWT token.
* `GET /actuator/health` — Spring Boot Actuator liveness and readiness health check.

### Secured Endpoints (`Authorization: Bearer <token>`)
* `POST /accounts` — Create a new account.
* `GET /accounts/{id}` — Fetch account details by ID.
* `GET /accounts?currency=USD` — Search accounts by currency (uses database derived query).
* `POST /accounts/{id}/credit` — Deposit funds into an account (`Idempotency-Key` supported).
* `POST /accounts/{id}/debit` — Withdraw funds from an account with balance validation and pessimistic locking.

---

## 📄 License
This project is open-source and available under the [MIT License](LICENSE).
