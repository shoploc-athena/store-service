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
14. [License](#license)

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
| Database | PostgreSQL (H2 in-memory for unit tests) |
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

# 3. Start the service
./mvnw spring-boot:run        # on Windows: mvnw.cmd spring-boot:run
```

The service listens on `http://localhost:8080`.

### Run with Docker Compose

The full stack (database, services, gateway) is started from the `shoploc-infra` repository.

## Configuration

All settings can be overridden with environment variables.

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/store_db` | JDBC URL of the database |
| `DB_USER` | `shoploc` | Database user |
| `DB_PASSWORD` | `shoploc` | Database password |

Never commit real secrets. Use environment variables or a local `.env` file (ignored by Git).

## API

> Planned endpoints, implemented step by step. See the project status below.

| Method | Path | Description |
|---|---|---|
| GET | `/api/products` | List products |

Full API documentation will be available in the project's GitHub Wiki.

## Running the tests

```bash
./mvnw verify
```

Unit and repository tests run against an in-memory H2 database, so no PostgreSQL is needed.

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
- Commits follow [Conventional Commits](https://www.conventionalcommits.org/), for example `feat(catalog): add product list endpoint [SHOP-12]`.

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow and review rules.

## CI/CD

A GitHub Actions pipeline runs on every push and pull request: build, tests, static analysis. On `main`, it also builds a Docker image and publishes it to the GitHub Container Registry.

## Eco-design

This project follows the French [RGESN](https://ecoresponsable.numerique.gouv.fr/publications/referentiel-general-ecoconception/) guidelines: minimal data collection, lightweight container images, CI workflows triggered only when needed, and no resources kept running when unused.

## Project status

| Item | Status |
|---|---|
| Project skeleton | In progress |
| Product endpoint | Planned |
| Dockerfile | Planned |
| CI pipeline | Planned |
| Static analysis | Planned |

## Team

Master MIAGE, Université de Lille.

- Marie
- Lan
- Brunine
- Angela

