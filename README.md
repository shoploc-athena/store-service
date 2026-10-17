# store-service

> Catalogue microservice of **ShopLoc**, a multi-city local commerce platform with click & collect, loyalty and merchant tools.
![CI](https://github.com/shoploc-athena/store-service/actions/workflows/ci.yml/badge.svg)
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
14. [Security](#security)

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
| Database | PostgreSQL (H2 in-memory for tests) |
| API documentation | springdoc-openapi (Swagger UI) |
| Build | Maven (Maven Wrapper included) |
| Container | Docker (multi-stage build), Docker Compose |
| Tests | JUnit 5, JaCoCo (coverage) |
| CI/CD | GitHub Actions |
| Static analysis | SonarCloud |
| Security | Dependabot, CodeQL, GitHub secret scanning |

## Getting started

There are three ways to run the service. Pick one.

| Mode | Base URL |
|---|---|
| Locally with Maven | `http://localhost:8080` |
| Docker Compose (recommended) | `http://localhost:8080` |
| Docker image alone | `http://localhost:8081` |

### Prerequisites

- Git
- Java 21 (JDK), only for the local Maven mode
- Docker Desktop

No Maven installation is needed: use the wrapper (`mvnw`). On Windows, use Git Bash or WSL, or `mvnw.cmd` instead of `./mvnw`.

### Option 1: run locally with Maven

```bash
# 1. Clone the repository
git clone https://github.com/shoploc-athena/store-service.git
cd store-service

# 2. Start a PostgreSQL database
docker run --name store-db -e POSTGRES_DB=store_db -e POSTGRES_USER=shoploc -e POSTGRES_PASSWORD=shoploc -p 5432:5432 -v store-db-data:/var/lib/postgresql/data -d postgres:16-alpine

# 3. Start the service
./mvnw spring-boot:run
```

These credentials are for local development only. Never reuse them in a real environment.

### Option 2: run with Docker Compose (recommended)

Docker Compose starts the database and the service together. The Compose file lives in the `shoploc-infra` repository, which builds the image from this repository. Clone both repositories **side by side** in the same parent folder:

```bash
git clone https://github.com/shoploc-athena/store-service.git
git clone https://github.com/shoploc-athena/shoploc-infra.git
cd shoploc-infra

# Create your local environment file, then set a local password in it
cp .env.example .env

docker compose up --build
```

Stop the platform with `docker compose down`. Add `-v` only if you also want to delete the database data.

The `.env` file contains secrets and is ignored by Git. Its variables are described in the [Configuration](#configuration) section.

### Option 3: run the Docker image alone

The image needs a database reachable on `localhost:5432` (see step 2 of Option 1).

```bash
docker build -t store-service:local .
docker run --rm -p 8081:8080 -e DB_URL=jdbc:postgresql://host.docker.internal:5432/store_db store-service:local
```

`host.docker.internal` lets the container reach the database running on your machine. It works with Docker Desktop on Mac and Windows. On Linux, add `--add-host=host.docker.internal:host-gateway` to the `docker run` command.

## Configuration

The service reads its database settings from environment variables. Defaults are provided for local development.

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/store_db` | JDBC URL of the database |
| `DB_USER` | `shoploc` | Database user |
| `DB_PASSWORD` | `shoploc` | Database password |

With Docker Compose, you do not set these variables yourself. Compose builds them from the `.env` file of `shoploc-infra`:

| `.env` variable | Description |
|---|---|
| `POSTGRES_DB` | Name of the database |
| `POSTGRES_USER` | Database user |
| `POSTGRES_PASSWORD` | Database password |

Never commit real secrets. Use environment variables or a local `.env` file (ignored by Git).

## API

| Method | Path | Description |
|---|---|---|
| GET | `/api/products` | List products |

### Documentation

Use the base URL of your running mode (see [Getting started](#getting-started)).

- **Swagger UI** (interactive): `<base URL>/swagger-ui/index.html`
- **OpenAPI description** (JSON): `<base URL>/v3/api-docs`
- **Technical documentation** (architecture, conventions, guides): [GitHub Wiki](https://github.com/shoploc-athena/store-service/wiki)

For example, with Docker Compose: `http://localhost:8080/swagger-ui/index.html`. With the Docker image alone: `http://localhost:8081/swagger-ui/index.html`.

## Running the tests

```bash
./mvnw verify
```

Unit and repository tests run against an in-memory H2 database, so no PostgreSQL is needed.

## Project structure

```text
store-service
├── .github/
│   ├── workflows/ci.yml      # CI pipeline
│   ├── dependabot.yml        # Dependency updates
│   └── pull_request_template.md                 
├── src/main/java/com/shoploc/store   # Application code
├── src/main/resources        # Configuration
├── src/test/java             # Tests
├── .dockerignore             # Files excluded from the image build
├── Dockerfile                # Container image
├── pom.xml                   # Maven build
└── README.md
```

## Branching and commits

- `main`: stable code, protected, changes only through pull requests.
- `develop`: integration branch.
- `feature/<JIRA-KEY>-short-description`: one branch per task.
- Commits follow [Conventional Commits](https://www.conventionalcommits.org/), for example `feat(catalog): add product list endpoint [GLOP-12]`.

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow and review rules.

## CI/CD

A GitHub Actions pipeline (`.github/workflows/ci.yml`) runs on every pull request and on every push to `main` and `develop`: it builds the project and runs the tests with coverage (`./mvnw verify`). On every push to `develop`, it also sends the analysis to SonarQube Cloud, which is limited to the main branch of the project by its free plan. The `build` check must pass before a pull request can be merged.

Publishing the Docker image to a registry is planned.

## Eco-design

This project follows the French [RGESN](https://ecoresponsable.numerique.gouv.fr/publications/referentiel-general-ecoconception/) guidelines: minimal data collection, lightweight container images, CI workflows triggered only when needed, and no resources kept running when unused.

## Security

See [SECURITY.md](SECURITY.md) to report a vulnerability. The repository is monitored by Dependabot (dependencies, GitHub Actions and Docker images), CodeQL (code analysis) and GitHub secret scanning with push protection.

## Project status

| Item | Status |
|---|---|
| Project skeleton | Done |
| Product endpoint | Done |
| API documentation (Swagger) | Done |
| Dockerfile | Done |
| Docker Compose (`shoploc-infra`) | Done |
| CI pipeline | Done |
| Static analysis | Done |
| Security scanning | Done |
| Authentication | Planned (`user-service`) |

## Team

Master MIAGE, Université de Lille.

- Marie
- Lan
- Brunine
- Angela