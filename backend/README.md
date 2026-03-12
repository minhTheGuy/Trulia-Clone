# Backend — Trulia Clone

Spring Boot microservices architecture. All services register with Eureka and communicate through the API Gateway.

## Services

| Service | Port | Responsibility |
|---|---|---|
| `service-registry` | 8761 | Eureka service discovery |
| `api-gateway` | 8080 | Single entry point, JWT validation, routing |
| `auth-service` | dynamic | Authentication, JWT issuance, OAuth2 (Google, GitHub), email verification, password reset |
| `user-service` | dynamic | User accounts, roles, saved searches, address management |
| `property-service` | dynamic | Property listings, categories, rentals, saved homes, property tours |
| `transaction-service` | dynamic | Stripe Checkout sessions, webhooks, payment records, Kafka event publishing |
| `file-service` | dynamic | Image and file upload/storage |

Services with `dynamic` ports use `server.port=0` and are discovered via Eureka.

## Structure

```
backend/
├── api-gateway/
│   └── src/
│       ├── config/
│       │   ├── GatewayConfig.java       # Route definitions
│       │   └── CorsConfig.java
│       └── filter/
│           └── JwtAuthenticationFilter  # Validates JWT on protected routes
├── auth-service/
│   └── src/
│       ├── controller/
│       │   ├── AuthController.java      # /api/auth/** (signin, signup, signout, refresh, verify)
│       │   ├── OAuthController.java     # /api/auth/oauth2/{provider}/authorize|callback
│       │   └── PasswordResetController.java
│       ├── security/
│       │   ├── jwt/JwtUtils.java        # Token generation and validation
│       │   └── WebSecurityConfig.java
│       ├── oauth2/OAuth2Service.java    # Google & GitHub OAuth2 flow
│       └── client/UserServiceClient.java  # Feign client → user-service
├── user-service/
│   └── src/
│       ├── controller/
│       │   ├── UserController.java      # /api/users/**
│       │   └── PublicAuthController.java  # /api/users/public/** (no JWT)
│       └── model/
│           ├── User.java
│           └── Role.java
├── property-service/
│   └── src/
│       ├── controller/
│       │   ├── PropertyController.java  # /api/properties/**
│       │   ├── RentalController.java    # /api/rentals/**
│       │   ├── SavedHomeController.java # /api/favorites/**
│       │   └── CategoryController.java  # /api/categories/**
│       └── model/
│           ├── Property.java
│           ├── PropertyCategory.java    # Enum — doubles as category service
│           └── Rental.java
├── transaction-service/
│   └── src/
│       ├── controller/
│       │   ├── StripeSessionController.java   # /api/stripe/** (create checkout session)
│       │   └── StripeWebhookController.java   # /api/webhook/stripe (Stripe events)
│       ├── service/impl/StripeServiceImpl.java
│       └── publisher/TransactionEventPublisher.java  # Publishes to Kafka
├── file-service/
│   └── src/
│       └── service/FileServiceImpl.java  # Stores files locally
├── service-registry/
│   └── src/ (Eureka server, no custom logic)
└── docker-compose.yml
```

## Infrastructure

**Database:** PostgreSQL 16 — each service with database needs shares one Postgres instance with a separate schema/database, configured via `spring.datasource.*` in each service's `application.properties`.

**Messaging:** Apache Kafka + Zookeeper — used by `transaction-service` to publish `TransactionEvent` messages after successful payments.

**Service discovery:** All services register with `service-registry` (Eureka). The API Gateway and inter-service Feign clients resolve addresses by service name.

## Running Locally

### Prerequisites
- Java 17
- Maven 3.9+
- Docker and Docker Compose (for Postgres, Kafka, Zookeeper)

### With Docker Compose

```bash
cd backend
docker-compose up --build
```

This starts Zookeeper, Kafka, PostgreSQL, and all seven services in dependency order.

### Individual service (dev mode)

Start `service-registry` first, then any service:

```bash
cd backend/service-registry
./mvnw spring-boot:run

cd backend/auth-service
./mvnw spring-boot:run
```

## Authentication

- JWT tokens are issued by `auth-service` and validated at the API Gateway (`JwtAuthenticationFilter`).
- Tokens carry `userId`, `email`, and `roles` as claims and are stored in localStorage by the frontend.
- OAuth2 (Google/GitHub) is handled via a custom Authorization Code flow in `OAuthController` + `OAuth2Service`. Configure client IDs and secrets in `auth-service/src/main/resources/application.properties`.

## Environment / Configuration

Each service has its own `application.properties`. Key values to change before running:

| Service | Property | Description |
|---|---|---|
| `auth-service` | `app.jwtSecret` | JWT signing secret |
| `auth-service` | `spring.mail.*` | SMTP credentials for email |
| `auth-service` | `oauth2.google.*` / `oauth2.github.*` | OAuth2 app credentials |
| `transaction-service` | `stripe.secret.key` | Stripe secret key |
| `transaction-service` | `stripe.webhook.secret` | Stripe webhook signing secret |
| All services | `spring.datasource.*` | PostgreSQL connection |
