# Trulia Clone

A full-stack real estate platform inspired by Trulia, built with a microservices backend and a React frontend. Users can browse, search, list, and transact on properties — with support for rentals, payments, and OAuth sign-in.

## Project Structure

```
Trulia-Clone/
├── backend/        # Spring Boot microservices
└── frontend/       # React + Vite application
```

## Tech Stack

**Backend** — Java 17, Spring Boot 3, Spring Cloud (Gateway, Eureka, OpenFeign), Spring Security + JWT, PostgreSQL, Stripe SDK, Apache Kafka, Docker

**Frontend** — React 19, Vite 6, Tailwind CSS, Redux Toolkit, React Router DOM v7, Axios, Stripe.js, Leaflet / Google Maps

## Getting Started

### With Docker (recommended)

```bash
# Start all backend services
cd backend
docker-compose up --build

# Start the frontend dev server
cd frontend
npm install && npm run dev
```

The API Gateway runs on `http://localhost:8080`, the frontend on `http://localhost:5173`.

### Running services individually

See [`backend/README.md`](./backend/README.md) and [`frontend/README.md`](./frontend/README.md) for per-service and frontend setup instructions.

## Key Features

- Property listings with advanced search filters (price, size, type, location)
- Role-based access — regular users, sellers, and admins
- Stripe Checkout for rental payments with webhook handling
- OAuth2 sign-in via Google and GitHub
- Email verification and password reset
- File/image uploads for property listings
- Saved homes and saved searches
- Kafka event streaming for transaction events
