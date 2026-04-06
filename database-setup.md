# Database Setup Guide

## Option 1: Docker (Recommended)

1. Install Docker from https://www.docker.com/

2. Run PostgreSQL container:
```bash
docker-compose up -d
```

3. Verify connection:
```bash
docker exec -it testerbook-db psql -U postgres -d testerbook
```

## Option 2: Local PostgreSQL Installation

### macOS (using Homebrew):
```bash
brew install postgresql
brew services start postgresql
psql postgres
CREATE DATABASE testerbook;
CREATE USER testerbook WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE testerbook TO testerbook;
```

### Ubuntu/Debian:
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo -u postgres psql
CREATE DATABASE testerbook;
CREATE USER testerbook WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE testerbook TO testerbook;
```

### Windows:
1. Download from https://www.postgresql.org/download/windows/
2. Install with password 'password'
3. Use pgAdmin to create database 'testerbook'

## Switch to PostgreSQL

Edit `src/main/resources/application.properties`:

```properties
# Comment out H2:
#spring.datasource.url=jdbc:h2:mem:testerbook

# Uncomment PostgreSQL:
spring.datasource.url=jdbc:postgresql://localhost:5432/testerbook
spring.datasource.username=postgres
spring.datasource.password=password
spring.datasource.driver-class-name=org.postgresql.Driver

# Update dialect:
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
```

## Restart Application

```bash
mvn spring-boot:run
```

The application will now use PostgreSQL with persistent data!
