# store-service

> Catalogue microservice of **ShopLoc**, a multi-city local commerce platform with click & collect, loyalty and merchant tools.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4-brightgreen)

## Table of contents

1. [About](#about)
2. [Responsibilities](#responsibilities)
3. [Tech stack](#tech-stack)
4. [Getting started](#getting-started)
5. [Configuration](#configuration)
6. [API](#api)
7. [Running the tests](#running-the-tests)
8. [Project structure](#project-structure)
9. [Branching and commits](#branching-and-commits)
10. [CI/CD](#cicd)
11. [Eco-design](#eco-design)
12. [Project status](#project-status)
13. [Team](#team)

## About

ShopLoc connects customers, local merchants, the city hall and the merchants' association. The platform is split into microservices, each living in its own repository. **store-service** owns everything about shops and what they sell.

Other services:

| Service | Purpose |
|---|---|
| `user-service` | Accounts, roles, authentication, loyalty (VFP status, points) |
| `store-service` | **This repository.** Shops, catalogue, stock, favourites |
| `order-service` | Cart, orders, payment, pick-up |
| `notification-analytics-service` | Notifications, statistics and reports |
| `shoploc-frontend` | Web application (React) and gateway |
| `shoploc-infra` | Docker Compose, diagrams, shared documentation |

## Responsibilities

- Manage shops and their catalogue (products and services).
- Manage stock levels.
- Manage customers' favourite shops (only the customer id is stored, never a link to another service's database).
- Expose a REST API consumed by the front end and by `order-service`.

## Tech stack

| Area | Choice |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot, Spring Web, Spring Data JPA |
| Database | PostgreSQL 16; Testcontainers for integration tests |
| Migrations | Flyway |
| Build | Maven (Maven Wrapper included) |
| Container | Docker (multi-stage build) |
| CI/CD | GitHub Actions |
| Quality | JUnit 5, Mockito, SonarCloud |

## Getting started

### Prerequisites

- Java 21 (JDK)
- Git
- Docker Desktop (to run PostgreSQL)

No Maven installation is needed: use the wrapper (`mvnw`).

### Run locally

```bash
# 1. Clone the repository
git clone https://github.com/shoploc-athena/store-service.git
cd store-service

# 2. Start a PostgreSQL database
docker run --name store-db -e POSTGRES_DB=store_db -e POSTGRES_USER=shoploc \
  -e POSTGRES_PASSWORD=shoploc -p 5432:5432 -d postgres:16-alpine

# 3. Set CITY and JWT_SECRET to the same values used by user-service.
#    JWT_SECRET must contain at least 32 UTF-8 bytes.

# 4. Start the service
./mvnw spring-boot:run        # on Windows: mvnw.cmd spring-boot:run
```

The service listens on `http://localhost:8080`.

**Note on database migrations:** Database schema is managed by Flyway. On first startup, all migrations in `src/main/resources/db/migration` are applied automatically. If you need to reset your local database:

```bash
docker stop store-db && docker rm store-db
# Then restart the container as shown above
```

### Run with Docker Compose

The full stack (database, services, gateway) is started from the `shoploc-infra` repository.

## Configuration

All settings can be overridden with environment variables.

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/store_db` | JDBC URL of the database |
| `DB_USER` | `shoploc` | Database user |
| `DB_PASSWORD` | `shoploc` | Database password |
| `CITY` | Required | City accepted in the authentication token (for example `lille`) |
| `JWT_SECRET` | Required | Shared HS256 secret used by `user-service`, at least 32 UTF-8 bytes |

Never commit real secrets. Use environment variables or a local `.env` file (ignored by Git).

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/products` | List products |
| POST | `/api/stores/{storeId}/articles` | Create an article and, for products, its initial stock |
| PUT | `/api/stores/{storeId}/articles/{id}` | Update an article, including publication status |
| GET | `/api/stores/{storeId}/articles?page=0&size=20` | List the owner's published and unpublished articles |

Article endpoints require a bearer JWT issued for the configured city by `user-service` and a merchant
whose user ID matches the store owner. Create requests contain `name` (1–150 characters), optional
`description`, `priceCents` (non-negative integer), `type` (`PRODUCT` or `SERVICE`) and optional
`published` (default `false`). Products may include `initialStock` (non-negative integer, default `0`);
services must not include it. Names are unique within each store.

Updates contain `name`, `description`, `priceCents`, `published`, and the current `version` from the
previous response; the article type cannot be changed. Duplicate names and stale versions return `409`.
There is no delete endpoint: set `published` to `false` to unpublish. Product responses include
`availableQuantity` and `reservedQuantity`. Authentication failures return `401`; a merchant who is
not the owner receives `403`. Prices are stored as integer cents.

The article API is stateless and accepts bearer tokens only in the `Authorization` header. It does not
authenticate using cookies, HTTP sessions, or token query parameters, so browsers do not automatically
attach authentication credentials to cross-site requests. CSRF protection is disabled for these endpoints
on that basis. If cookie- or session-based authentication is added, CSRF protection must be reconsidered
before enabling it.

### Documentation

- **Swagger UI** (interactive, available when the service is running): `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI description** (JSON): `http://localhost:8080/v3/api-docs`
- **Technical documentation** (architecture, conventions, guides): [GitHub Wiki](https://github.com/shoploc-athena/store-service/wiki)

## Running the tests

```bash
./mvnw verify
```

Integration and repository tests run against a PostgreSQL container managed by Testcontainers. Docker is required to run tests. The first run downloads the PostgreSQL container image and may take a minute or two.

## Project structure

```text
store-service
├── .github/                  # CI workflows, PR template
├── src/main/java/com/shoploc/store   # Application code
├── src/main/resources        # Configuration
├── src/test/java             # Tests
├── Dockerfile                # Container image
├── pom.xml                   # Maven build
└── README.md
```

## Branching and commits

- `main`: stable code, protected, changes only through pull requests.
- `develop`: integration branch.
- `feature/<JIRA-KEY>-short-description`: one branch per task.
- Commits follow [Conventional Commits](https://www.conventionalcommits.org/), for example `feat(catalog): add product list endpoint [GLOP-33]`.

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow and review rules.

## CI/CD

A GitHub Actions pipeline runs on every push and pull request: build, tests, static analysis. On `main`, it also builds a Docker image and publishes it to the GitHub Container Registry.

## Eco-design

This project follows the French [RGESN](https://ecoresponsable.numerique.gouv.fr/publications/referentiel-general-ecoconception/) guidelines: minimal data collection, lightweight container images, CI workflows triggered only when needed, and no resources kept running when unused.

## Project status

| Item | Status |
|---|---|
| Project skeleton | In progress |
| Product endpoint | Done |
| Dockerfile | Done |
| CI pipeline | Done |
| Static analysis | Done |
| Database migrations (Flyway) | In progress |

## Team

Master MIAGE, Université de Lille.

- Marie
- Lan
- Brunine
- Angela

