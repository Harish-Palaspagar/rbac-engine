# Dynamic RBAC - Role-Based Access Control with Spring Boot

A production-ready implementation of dynamic Role-Based Access Control (RBAC) using Spring Boot, Spring Security, and JPA. Permissions are stored in the database and evaluated at runtime with no hardcoded role authorities in source code.

## Table of Contents

- [Overview](#overview)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Authorization Flow](#authorization-flow)
- [Seeded Data](#seeded-data)
- [API Endpoints](#api-endpoints)
- [Production Deployment](#production-deployment)
- [Recent Progress](#recent-progress)
- [Local Run](#local-run)

## Overview

This project implements a dynamic RBAC system where:

- Users are assigned one or more roles
- Roles are assigned one or more permissions
- Each protected API declares the permission it requires
- Permissions are looked up from the database on every request
- Roles and permissions can be changed without redeploying the app

The app now runs both locally and in production on AWS Elastic Beanstalk.

## Technology Stack

| Technology | Version | Purpose |
|---|---:|---|
| Java | 21 | Language runtime |
| Spring Boot | 4.0.5 | Application framework |
| Spring Security | 7.0.4 | Authentication and authorization |
| Spring Data JPA | 4.0.4 | Persistence layer |
| Hibernate | 7.2.7 | ORM |
| H2 | 2.4.240 | Local development database |
| PostgreSQL | 17.x | Production database on AWS RDS |
| Maven Wrapper | included | Build and test |
| GitHub Actions | current | CI/CD |
| AWS Elastic Beanstalk | Corretto 21 / AL2023 | Production hosting |

## Project Structure

```text
src/main/java/com/rbac/
|-- config/
|   |-- DefaultDataSeeder.java
|   |-- MethodSecurityConfig.java
|   `-- SecurityConfig.java
|-- controller/
|   |-- HealthController.java
|   |-- PermissionController.java
|   |-- RoleController.java
|   |-- SecureDataController.java
|   `-- UserController.java
|-- dto/
|   |-- AssignmentResponse.java
|   |-- ErrorResponse.java
|   |-- PermissionRequest.java
|   |-- PermissionResponse.java
|   |-- RoleRequest.java
|   |-- RoleResponse.java
|   |-- SecureDataResponse.java
|   |-- UserCreateRequest.java
|   `-- UserResponse.java
|-- entity/
|-- exception/
|-- repository/
|-- security/
|-- service/
`-- DynamicRbacApplication.java
```

## Authorization Flow

1. A request authenticates with HTTP Basic auth.
2. `UserDetailsServiceImpl` loads the user from the database.
3. `SecurityConfig` requires authentication for all routes except explicitly allowed ones like `/healthz`.
4. `@PreAuthorize("hasPermission(null, '...')")` delegates to `DynamicPermissionEvaluator`.
5. `DynamicPermissionEvaluator` fetches the user's role IDs, then fetches the permission names for those roles.
6. Access is granted only if the requested permission is present in the database-derived set.

This means permission changes take effect on the next request without requiring the user to log out or the app to restart.

## Seeded Data

Seed data is created by `DefaultDataSeeder` when `app.seed.enabled=true`. In production, set `APP_SEED_ENABLED=false`.

Default users:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `user1` | `user123` | `USER` |
| `user2` | `user123` | none |

Default roles:

- `ADMIN`
- `USER`

Default permissions:

- `MANAGE_ROLES`
- `MANAGE_PERMISSIONS`
- `MANAGE_USERS`
- `ASSIGN_PERMISSIONS`
- `ASSIGN_ROLES`
- `ACCESS_SECURE_DATA`

Default role assignments:

- `ADMIN` -> `MANAGE_ROLES`, `MANAGE_PERMISSIONS`, `MANAGE_USERS`, `ASSIGN_PERMISSIONS`, `ASSIGN_ROLES`
- `USER` -> `ACCESS_SECURE_DATA`

## API Endpoints

All protected endpoints use HTTP Basic authentication.

### Permissions

| Method | URL | Permission | Description |
|---|---|---|---|
| `POST` | `/permissions` | `MANAGE_PERMISSIONS` | Create a permission |
| `GET` | `/permissions` | `MANAGE_PERMISSIONS` | List permissions |
| `GET` | `/permissions/{id}` | `MANAGE_PERMISSIONS` | Get permission by ID |

### Roles

| Method | URL | Permission | Description |
|---|---|---|---|
| `POST` | `/roles` | `MANAGE_ROLES` | Create a role |
| `GET` | `/roles` | `MANAGE_ROLES` | List roles |
| `GET` | `/roles/{id}` | `MANAGE_ROLES` | Get role by ID |
| `POST` | `/roles/{roleId}/permissions/{permissionId}` | `ASSIGN_PERMISSIONS` | Assign permission to role |

### Users

| Method | URL | Permission | Description |
|---|---|---|---|
| `POST` | `/users` | `MANAGE_USERS` | Create a new user |
| `POST` | `/users/{userId}/roles/{roleId}` | `ASSIGN_ROLES` | Assign a role to a user |

### Protected Demo

| Method | URL | Permission | Description |
|---|---|---|---|
| `GET` | `/secure-data` | `ACCESS_SECURE_DATA` | Protected sample endpoint |

### Public Health Check

| Method | URL | Auth | Description |
|---|---|---|---|
| `GET` | `/healthz` | none | Public deployment verification endpoint |

Expected `/healthz` response:

```json
{
  "service": "dynamic-rbac",
  "status": "ok",
  "release": "healthz-v2"
}
```

## Production Deployment

The application is deployed to AWS Elastic Beanstalk through GitHub Actions.

Current production target:

- Application: `dynamic-rbac`
- Environment: `dynamic-rbac-prod`
- Region: `ap-south-1`
- URL: `http://dynamic-rbac-prod.ap-south-1.elasticbeanstalk.com`

Deployment flow:

1. Push to `main`
2. GitHub Actions runs tests
3. Maven packages the JAR
4. The workflow builds an Elastic Beanstalk bundle with `application.jar` and `Procfile`
5. The bundle is uploaded to S3
6. A new Elastic Beanstalk application version is created
7. Elastic Beanstalk updates `dynamic-rbac-prod`

Production configuration:

- Spring profile: `prod`
- Database: PostgreSQL on AWS RDS
- Health verification: `/healthz`
- Environment variables are configured in Elastic Beanstalk

GitHub Actions and AWS setup details are documented in [.github/workflows/README.md](C:/Users/dbrin/OneDrive/Desktop/RBAC/dynamic-rbac/.github/workflows/README.md:1).

## Recent Progress

What has been achieved in this repository:

- Dynamic database-driven RBAC implemented
- Production deployment to AWS Elastic Beanstalk completed
- GitHub Actions CI/CD pipeline created and working
- OIDC-based AWS role assumption from GitHub Actions configured
- Production PostgreSQL wiring through Elastic Beanstalk environment variables
- Public `/healthz` endpoint added for deployment verification
- Missing-route handling corrected so unmapped routes return `404` instead of generic `500`
- Admin-only `POST /users` endpoint added through the `MANAGE_USERS` permission
- Tests added for health endpoint, user creation flow, and seed updates

## Local Run

Prerequisites:

- Java 21+

Run locally:

```bash
./mvnw clean test
./mvnw spring-boot:run
```

Local app URL:

- `http://localhost:8080`

Useful local checks:

```bash
curl http://localhost:8080/healthz
curl -u admin:admin123 http://localhost:8080/roles
curl -u user1:user123 http://localhost:8080/secure-data
```

Example user creation request:

```bash
curl -X POST http://localhost:8080/users \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"newuser\",\"password\":\"secret123\"}"
```

Expected response:

```json
{
  "id": 4,
  "username": "newuser",
  "enabled": true
}
```
