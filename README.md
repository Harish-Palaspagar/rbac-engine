# Dynamic RBAC — Role-Based Access Control with Spring Boot

A production-ready implementation of dynamic Role-Based Access Control (RBAC) using Spring Boot, Spring Security, and JPA. Permissions are stored in the database and evaluated at runtime — no hardcoded roles in source code.

---

## Table of Contents

- [Overview](#overview)
- [Why Dynamic RBAC](#why-dynamic-rbac)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Database Schema](#database-schema)
- [Authorization Flow](#authorization-flow)
- [Permission Evaluation Logic](#permission-evaluation-logic)
- [How PermissionEvaluator Is Used](#how-permissionevaluator-is-used)
- [Why Hardcoded Roles Are Avoided](#why-hardcoded-roles-are-avoided)
- [Example Permission Checks](#example-permission-checks)
- [Seeded Data](#seeded-data)
- [API Endpoints](#api-endpoints)
- [Steps to Run the Application](#steps-to-run-the-application)

---

## Overview

This project implements a fully dynamic RBAC system where:

- Users are assigned one or more roles
- Roles are assigned one or more permissions
- Every API endpoint declares which permission is required
- At runtime, the system queries the database to determine if the authenticated user holds the required permission
- Roles and permissions can be created and assigned without modifying or restarting the application

---

## Why Dynamic RBAC

Traditional Spring Security setups hardcode role checks like:

```java
@PreAuthorize("hasRole('ADMIN')")
```

This approach has serious limitations:

- Adding a new role requires a code change and redeployment
- You cannot assign fine-grained permissions per role at runtime
- Business teams cannot manage access control without developer involvement

This project solves all of that by storing the entire permission model in the database and evaluating it dynamically on every request.

---

## Technology Stack

| Technology           | Version | Purpose                            |
|----------------------|---------|------------------------------------|
| Java                 | 21      | Programming language               |
| Spring Boot          | 4.0.5   | Application framework              |
| Spring Security      | 7.0.4   | Authentication and authorization   |
| Spring Data JPA      | 4.0.4   | Database access layer              |
| Hibernate            | 7.2.7   | ORM provider                       |
| H2 Database          | 2.4.240 | In-memory database for development |
| Lombok               | 1.18.44 | Boilerplate reduction              |
| Jakarta Validation   | 3.1.1   | Request body validation            |

---

## Project Structure

```
src/main/java/com/rbac/
├── DynamicRbacApplication.java       Application entry point
│
├── config/
│   ├── SecurityConfig.java           HTTP security, BCrypt, filter chain
│   └── MethodSecurityConfig.java     Wires DynamicPermissionEvaluator
│
├── controller/
│   ├── RoleController.java           Role CRUD + permission assignment
│   ├── PermissionController.java     Permission CRUD
│   ├── UserController.java           Role assignment to users
│   └── SecureDataController.java     Protected demo endpoint
│
├── dto/
│   ├── RoleRequest.java / RoleResponse.java
│   ├── PermissionRequest.java / PermissionResponse.java
│   ├── AssignmentResponse.java
│   ├── SecureDataResponse.java
│   └── ErrorResponse.java
│
├── entity/
│   ├── User.java                     Maps to app_user table
│   ├── Role.java                     Maps to role table
│   ├── Permission.java               Maps to permission table
│   ├── UserRole.java                 Join table: user <-> role
│   └── RolePermission.java           Join table: role <-> permission
│
├── exception/
│   ├── RbacExceptions.java           Custom exception types
│   └── GlobalExceptionHandler.java   Centralized error handling
│
├── repository/
│   ├── UserRepo.java
│   ├── RoleRepo.java
│   ├── PermissionRepo.java
│   ├── UserRoleRepo.java             Custom JPQL queries
│   └── RolePermissionRepo.java       Custom JPQL queries
│
└── security/
    ├── CustomUserDetails.java        Wraps User entity, exposes userId
    ├── UserDetailsServiceImpl.java   Loads user from DB during auth
    └── DynamicPermissionEvaluator.java   Core permission evaluation logic
```

---

## Database Schema

```
app_user          user_role         role          role_permission      permission
-----------       ----------        --------      ---------------      ----------
id (PK)      <--- user_id (FK)      id (PK)  <--- role_id (FK)         id (PK)
username          role_id (FK) ---> name          permission_id (FK)-> name
password          id (PK)                         id (PK)
enabled
```

The schema uses two explicit join tables (`user_role` and `role_permission`) instead of JPA's `@ManyToMany`. This gives full control over the join tables, allows additional fields to be added later (such as assigned date), and makes queries more explicit.

---

## Authorization Flow

Every incoming HTTP request goes through the following stages in order:

### Stage 1 — HTTP Request with Basic Auth

```
POST /roles
Authorization: Basic YWRtaW46YWRtaW4xMjM=   (Base64 of admin:admin123)
Body: {"name": "MANAGER"}
```

### Stage 2 — Spring Security Filter Chain

The request passes through the security filter chain before reaching any controller:

```
BasicAuthenticationFilter
    Decodes Base64 header
    Extracts username and password
    Calls UserDetailsServiceImpl.loadUserByUsername()
```

### Stage 3 — Load User from Database

```java
// UserDetailsServiceImpl
User user = userRepository.findByUsername("admin");
return new CustomUserDetails(user);
// SQL: SELECT * FROM app_user WHERE username = 'admin'
```

### Stage 4 — Password Verification

```
BCryptPasswordEncoder.matches("admin123", storedHash)
    Match → Authentication succeeds
    No match → 401 Unauthorized, request stops here
```

### Stage 5 — Authentication Object Stored

Spring stores the authenticated user in `SecurityContextHolder` for use throughout the request lifecycle.

### Stage 6 — URL-Level Authorization

`SecurityConfig` checks:
```
/h2-console/** → permitAll
Any other URL  → must be authenticated
```

### Stage 7 — Method-Level Authorization (@PreAuthorize)

AOP intercepts the controller method before it runs:
```
@PreAuthorize("hasPermission(null, 'MANAGE_ROLES')")
    Evaluates SpEL expression
    Calls DynamicPermissionEvaluator.hasPermission()
```

### Stage 8 — Database Permission Check

`DynamicPermissionEvaluator` queries the database:
```
Step 1: Get role IDs for the user
        SELECT role_id FROM user_role WHERE user_id = 1
        Result: [1]

Step 2: Get all permission names for those roles
        SELECT p.name FROM role_permission rp
        JOIN permission p ON p.id = rp.permission_id
        WHERE rp.role_id IN (1)
        Result: ["MANAGE_ROLES", "MANAGE_PERMISSIONS", "ASSIGN_PERMISSIONS", "ASSIGN_ROLES"]

Step 3: Check if required permission is in the list
        ["MANAGE_ROLES", ...].contains("MANAGE_ROLES") → true
```

### Stage 9 — Result

```
Permission granted  → Controller method executes → 201 Created
Permission denied   → AccessDeniedException thrown → 403 Forbidden
```

---

## Permission Evaluation Logic

The core logic lives in `DynamicPermissionEvaluator`:

```java
@Component
@RequiredArgsConstructor
public class DynamicPermissionEvaluator implements PermissionEvaluator {

    private final UserRoleRepo userRoleRepository;
    private final RolePermissionRepo rolePermissionRepository;

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomainObject,
                                 Object permission) {

        // Cast permission to String
        if (!(permission instanceof String requiredPermission)) return false;

        // Get our custom user details which contain userId
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) return false;

        // Query 1: find which roles this user has
        List<Long> roleIds = userRoleRepository.findRoleIdsByUserId(userDetails.getUserId());

        // Query 2: find all permission names for those roles
        List<String> grantedPermissions = rolePermissionRepository
                .findPermissionNamesByRoleIds(roleIds);

        // Check if required permission is granted
        return grantedPermissions.contains(requiredPermission);
    }
}
```

The evaluator performs exactly two database queries on every permission check:

1. `SELECT role_id FROM user_role WHERE user_id = ?`
2. `SELECT p.name FROM role_permission JOIN permission WHERE role_id IN (?)`

This is intentional — it ensures permissions are always fresh from the database. If you assign a new permission via the API, it takes effect on the very next request.

---

## How PermissionEvaluator Is Used

`DynamicPermissionEvaluator` is registered in `MethodSecurityConfig`:

```java
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class MethodSecurityConfig {

    private final DynamicPermissionEvaluator dynamicPermissionEvaluator;

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler =
                new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(dynamicPermissionEvaluator);
        return handler;
    }
}
```

`@EnableMethodSecurity(prePostEnabled = true)` activates support for `@PreAuthorize` and `@PostAuthorize` on controller and service methods.

`handler.setPermissionEvaluator(dynamicPermissionEvaluator)` tells Spring: when a `@PreAuthorize` expression contains `hasPermission(...)`, delegate evaluation to `DynamicPermissionEvaluator`.

On every controller method annotated with `@PreAuthorize("hasPermission(null, 'SOME_PERMISSION')")`:

- Spring AOP intercepts the method call before execution
- Evaluates the SpEL expression
- Calls `DynamicPermissionEvaluator.hasPermission(authentication, null, "SOME_PERMISSION")`
- If it returns `false`, throws `AccessDeniedException`
- `GlobalExceptionHandler` catches it and returns `403 Forbidden`

---

## Why Hardcoded Roles Are Avoided

The standard Spring Security approach uses:

```java
@PreAuthorize("hasRole('ADMIN')")
```

This has the following drawbacks:

1. **Code change required for new roles** — adding a MANAGER role that can only manage permissions requires a developer to update and redeploy the code.

2. **No fine-grained permission control** — `hasRole('ADMIN')` gives the entire ADMIN role access to everything annotated with it. You cannot say "ADMIN has MANAGE_ROLES but not DELETE_USERS" without code changes.

3. **Authorities must be loaded at login** — Spring's built-in role checks rely on the `getAuthorities()` method of `UserDetails`, which means all roles must be loaded and stored at the time of authentication. If roles change, the user must log out and log back in.

This project avoids all of these issues:

- `CustomUserDetails.getAuthorities()` intentionally returns an empty list
- No roles are loaded at login time
- Permissions are checked dynamically against the database on every request
- New roles and permissions can be created via API without touching source code
- A role's permissions can be modified at runtime and take effect immediately

---

## Example Permission Checks

### Endpoint requires MANAGE_ROLES permission

```java
@PostMapping
@PreAuthorize("hasPermission(null, 'MANAGE_ROLES')")
public ResponseEntity<RoleResponse> createRole(@Valid @RequestBody RoleRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(roleService.createRole(request));
}
```

When admin (who has ADMIN role with MANAGE_ROLES permission) calls this:
```
Permission check: ["MANAGE_ROLES", "MANAGE_PERMISSIONS", "ASSIGN_PERMISSIONS", "ASSIGN_ROLES"]
                   .contains("MANAGE_ROLES") → true → 201 Created
```

When user1 (who has USER role with ACCESS_SECURE_DATA permission) calls this:
```
Permission check: ["ACCESS_SECURE_DATA"].contains("MANAGE_ROLES") → false → 403 Forbidden
```

### Endpoint requires ACCESS_SECURE_DATA permission

```java
@GetMapping("/secure-data")
@PreAuthorize("hasPermission(null, 'ACCESS_SECURE_DATA')")
public ResponseEntity<SecureDataResponse> getSecureData(
        @AuthenticationPrincipal CustomUserDetails principal) { ... }
```

- `user1` with USER role → has ACCESS_SECURE_DATA → 200 OK
- `admin` with ADMIN role → does NOT have ACCESS_SECURE_DATA → 403 Forbidden
- `user2` with no role → no permissions at all → 403 Forbidden

### Endpoint requires ASSIGN_PERMISSIONS permission

```java
@PostMapping("/{roleId}/permissions/{permissionId}")
@PreAuthorize("hasPermission(null, 'ASSIGN_PERMISSIONS')")
public ResponseEntity<AssignmentResponse> assignPermissionToRole(...) { ... }
```

Only admin can call this because ADMIN role has the ASSIGN_PERMISSIONS permission.

---

## Seeded Data

The application seeds the following data on startup via `data.sql`:

### Users

| ID | Username | Password   |
|----|----------|------------|
| 1  | admin    | admin123   |
| 2  | user1    | user123    |
| 3  | user2    | user123    |

### Roles

| ID | Name  |
|----|-------|
| 1  | ADMIN |
| 2  | USER  |

### Permissions

| ID | Name                |
|----|---------------------|
| 1  | MANAGE_ROLES        |
| 2  | MANAGE_PERMISSIONS  |
| 3  | ASSIGN_PERMISSIONS  |
| 4  | ASSIGN_ROLES        |
| 5  | ACCESS_SECURE_DATA  |

### Role — Permission Assignments

| Role  | Permissions                                                              |
|-------|--------------------------------------------------------------------------|
| ADMIN | MANAGE_ROLES, MANAGE_PERMISSIONS, ASSIGN_PERMISSIONS, ASSIGN_ROLES       |
| USER  | ACCESS_SECURE_DATA                                                       |

### User — Role Assignments

| User  | Role  |
|-------|-------|
| admin | ADMIN |
| user1 | USER  |
| user2 | (none — assign via API) |

---

## API Endpoints

All endpoints use HTTP Basic Authentication.

### Role Endpoints

| Method | URL                                    | Permission Required  | Description                  |
|--------|----------------------------------------|----------------------|------------------------------|
| POST   | /roles                                 | MANAGE_ROLES         | Create a new role             |
| GET    | /roles                                 | MANAGE_ROLES         | List all roles                |
| GET    | /roles/{id}                            | MANAGE_ROLES         | Get role by ID                |
| POST   | /roles/{roleId}/permissions/{permId}   | ASSIGN_PERMISSIONS   | Assign permission to role     |

### Permission Endpoints

| Method | URL                  | Permission Required    | Description               |
|--------|----------------------|------------------------|---------------------------|
| POST   | /permissions         | MANAGE_PERMISSIONS     | Create a new permission   |
| GET    | /permissions         | MANAGE_PERMISSIONS     | List all permissions      |
| GET    | /permissions/{id}    | MANAGE_PERMISSIONS     | Get permission by ID      |

### User Endpoints

| Method | URL                              | Permission Required | Description              |
|--------|----------------------------------|---------------------|--------------------------|
| POST   | /users/{userId}/roles/{roleId}   | ASSIGN_ROLES        | Assign role to user      |

### Protected Demo Endpoint

| Method | URL           | Permission Required  | Description                        |
|--------|---------------|----------------------|------------------------------------|
| GET    | /secure-data  | ACCESS_SECURE_DATA   | Returns data accessible by USER role |

### H2 Console

| URL          | Auth Required | Description         |
|--------------|---------------|---------------------|
| /h2-console  | No            | H2 database browser |

---

## Steps to Run the Application

### Prerequisites

- Java 21 or higher installed
- Maven 3.6 or higher installed

Verify with:
```bash
java -version
mvn -version
```

### Step 1 — Clone the Repository

```bash
git clone https://github.com/your-username/dynamic-rbac.git
cd dynamic-rbac
```

### Step 2 — Build the Project

```bash
mvn clean compile
```

### Step 3 — Run the Application

```bash
mvn spring-boot:run
```

Or run directly from your IDE by executing `DynamicRbacApplication.java`.

The application starts on `http://localhost:8080`.

You will see this in the logs:
```
Tomcat initialized with port 8080 (http)
HikariPool-1 - Start completed.
Started DynamicRbacApplication
```

### Step 4 — Verify Startup

Open the H2 console to verify the database was seeded:
```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:rbacdb
Username: username
Password: password
```

Run this query to confirm seed data:
```sql
SELECT u.username, r.name AS role, p.name AS permission
FROM app_user u
JOIN user_role ur ON ur.user_id = u.id
JOIN role r ON r.id = ur.role_id
JOIN role_permission rp ON rp.role_id = r.id
JOIN permission p ON p.id = rp.permission_id;
```

### Step 5 — Test with Postman or curl

**Create a new role (as admin):**
```bash
curl -X POST http://localhost:8080/roles \
  -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"name": "MANAGER"}'
```

Expected response:
```json
HTTP 201 Created
{
    "id": 3,
    "name": "MANAGER"
}
```

**Access secure data (as user1):**
```bash
curl -X GET http://localhost:8080/secure-data \
  -u user1:user123
```

Expected response:
```json
HTTP 200 OK
{
    "message": "Access granted to secure resource",
    "accessedBy": "user1",
    "timestamp": 1713780000000,
    "note": "This endpoint is guarded by the ACCESS_SECURE_DATA permission, evaluated dynamically from the database at runtime."
}
```

**Try accessing a restricted endpoint with the wrong user:**
```bash
curl -X POST http://localhost:8080/roles \
  -u user1:user123 \
  -H "Content-Type: application/json" \
  -d '{"name": "VIEWER"}'
```

Expected response:
```json
HTTP 403 Forbidden
{
    "status": 403,
    "error": "Forbidden",
    "message": "You do not have the required permission to perform this action."
}
```

### Step 6 — Assign a Role to user2 (as admin)

```bash
curl -X POST http://localhost:8080/users/3/roles/2 \
  -u admin:admin123
```

This assigns the USER role (id=2) to user2 (id=3). After this, user2 can access `/secure-data`.

### Notes

- The H2 database is in-memory. All data resets when the application restarts.
- `spring.jpa.hibernate.ddl-auto=create-drop` means Hibernate creates tables on startup and drops them on shutdown.
- `spring.jpa.defer-datasource-initialization=true` ensures `data.sql` runs after Hibernate creates the schema.
- Passwords in `data.sql` are BCrypt hashed at strength 10.
