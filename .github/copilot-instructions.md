# QuanLyThuVien-BE — Project Guidelines

Library Management System backend. Spring Boot 4.0.1 + MongoDB, stateless JWT auth, Maven build.

## Build & Run

```bash
# Start all services (Spring Boot on :8081, MongoDB, Mongo Express on :8082)
docker compose up -d --build

# Build without Docker
./mvnw clean package -DskipTests

# Run tests
./mvnw test
```

Credentials for Mongo Express UI (`http://localhost:8082`): `admin` / `admin`.

> **Known port issue**: `docker-compose.yml` maps `8081:8080` but the app listens on `8081` — fix by changing the mapping to `8081:8081`.

Environment variable required: `SPRING_DATA_MONGODB_URI` (loaded via `dotenv-java` from a `.env` file in project root).

## Architecture

Feature-based packages under `com.dev`, each with `controller / dto / model / repository / service`:

```
com.dev
├── auth/      # JWT auth, registration, login, refresh, logout, profile
├── book/      # CRUD for library books (paginated listing)
├── borrow/    # UNIMPLEMENTED — directories exist but all files are empty
├── user/      # User model & repository only; no controller yet
├── config/    # SecurityConfig (global filter chain)
└── exception/ # GlobalExceptionHandler, ErrorResponse
```

Layer order: `Controller → Service interface → ServiceImpl → Repository → @Document model`

All services follow the interface + implementation pattern (`FooService` / `FooServiceImpl`).

## Code Conventions

- **Jakarta EE** namespace (`jakarta.*`), not `javax.*` — Spring Boot 4.x.
- **Lombok** everywhere: `@Data`, `@Builder`, `@RequiredArgsConstructor`, `@NoArgsConstructor`/`@AllArgsConstructor`.
- **Constructor injection** via `@RequiredArgsConstructor` — do not use `@Autowired` field injection.
- **MongoDB**: `@Document("collection_name")` on models, `@Id` fields are `String` (MongoDB ObjectId).
- **DTOs**: Request DTOs use Bean Validation (`@NotBlank`, `@Email`, etc.). Response DTOs use `@Builder` where mapping is non-trivial. Map manually via private `mapToResponse()` — no MapStruct.
- **Validation**: Always annotate `@RequestBody` params with `@Valid` in controllers.
- Vietnamese is acceptable in comments and user-facing error messages.

## Security

Stateless JWT (JJWT 0.11.5). Access token: 1 h. Refresh token: 7 days, persisted in MongoDB and rotated on each refresh.

| Route                 | Access                      |
| --------------------- | --------------------------- |
| `/api/auth/**`        | Public                      |
| `/api/admin/**`       | `ROLE_ADMIN`                |
| `/api/user/**`        | `ROLE_USER` or `ROLE_ADMIN` |
| `/api/books/admin/**` | `ROLE_ADMIN`                |
| `/api/borrows/**`     | Public (placeholder)        |
| Everything else       | Authenticated               |

Roles: `ADMIN`, `LIBRARIAN`, `USER`. `LIBRARIAN` is defined but has no route rules yet.

On logout, the access token is blacklisted in the `blacklist_tokens` collection; `JwtAuthFilter` checks the blacklist on every request.

> **Security todo**: JWT secret is currently hardcoded in `JwtService` — move to `SPRING_JWT_SECRET` env var before production.

## Exception Handling

`GlobalExceptionHandler` (`@RestControllerAdvice`) handles:

- `MethodArgumentNotValidException` → 400 with `VALIDATION_ERROR`
- `RuntimeException` → 400 with the exception message

For new service errors, throw `RuntimeException` with a descriptive Vietnamese message.

## Unimplemented Areas

- `borrow/` module: model, DTO, repository, service, and controller are all empty.
- `user/controller/` is empty — no user management endpoints beyond auth.
- `LIBRARIAN` role has no associated route rules or business logic.
