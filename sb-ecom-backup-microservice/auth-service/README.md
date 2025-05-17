# Auth Service

Authentication and Authorization service for the E-commerce microservices system.

## Overview

The Auth Service is responsible for:
- User authentication (sign-in)
- User registration (sign-up)
- JWT token generation and validation
- User profile information

This service acts as a thin layer over the user-service for authentication purposes.

## Architecture

The Auth Service is built as a microservice with the following components:
- Spring Boot 3.x with Java 17
- Spring Security
- JWT for token-based authentication
- Spring Cloud OpenFeign for service-to-service communication
- Eureka for service discovery

## API Endpoints

### Authentication

- `POST /api/auth/signin` - Authenticate a user
- `POST /api/auth/signup` - Register a new user
- `POST /api/auth/signout` - Log out a user

### User Information

- `GET /api/auth/profile` - Get the current authenticated user's profile
- `GET /api/auth/validate` - Validate a JWT token

### Public Endpoints

- `GET /api/auth/public/test` - Public test endpoint (no authentication required)

## Configuration

Configuration is managed through Spring Cloud Config Server. Main properties:

- `app.jwtSecret` - Secret key for JWT signing
- `app.jwtExpirationMs` - Token expiration time in milliseconds
- `app.jwtCookieName` - Name of the cookie used to store the JWT
- `frontend.url` - Frontend URL for CORS configuration
- `user-service.url` - URL of the user service
- Other service URLs for OpenFeign clients

## Usage

### Authentication Flow

1. Client sends credentials to `/api/auth/signin`
2. Auth service forwards the request to user-service for validation
3. If valid, auth-service generates a JWT token and returns it
4. Client includes the token in subsequent requests
5. Other services use the token for authorization

### Dependencies

This service depends on:
- User Service - For user authentication and user data
- Property Service - For property data
- Transaction Service - For transaction data

## Building and Running

```bash
# Build
mvn clean package

# Run
java -jar target/auth-service-0.0.1-SNAPSHOT.jar
``` 