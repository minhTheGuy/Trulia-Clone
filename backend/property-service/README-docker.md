# Property Service Database Setup

This guide explains how to use Docker Compose to set up the PostgreSQL database for the property service.

## Prerequisites

- Docker and Docker Compose installed on your system
- Basic knowledge of Docker and Docker Compose

## Quick Start

1. Navigate to the property-service directory:
   ```
   cd property-service
   ```

2. Start the PostgreSQL container:
   ```
   docker-compose up -d
   ```

3. The PostgreSQL server will be available at:
   - Host: localhost
   - Port: 5432
   - Database: ecommerce_properties
   - Username: postgres
   - Password: password

4. Access PgAdmin (database management UI) at:
   - URL: http://localhost:5050
   - Email: admin@example.com
   - Password: admin

## Connect to PostgreSQL

When PgAdmin loads, add a new server with these settings:
- Name: Property Service DB
- Host: postgres
- Port: 5432
- Username: postgres
- Password: password

## Managing the Container

- **Start containers**:
  ```
  docker-compose up -d
  ```

- **Stop containers**:
  ```
  docker-compose down
  ```

- **View logs**:
  ```
  docker-compose logs
  ```

- **View running containers**:
  ```
  docker-compose ps
  ```

## Data Persistence

Database data is persisted in a Docker volume named `property-postgres-data`. This ensures your data remains intact even if the container is stopped or removed.

## Troubleshooting

- If port 5432 is already in use, modify the port mapping in docker-compose.yml.
- Ensure no other PostgreSQL instances are running on your system. 