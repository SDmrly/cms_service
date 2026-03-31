# Multi-Language CMS Service

A Spring Boot–based, multi-language Content Management System (CMS) backed by PostgreSQL and Redis. Create projects, manage CMS keys, handle translations, and authenticate users through a secure JWT-based auth module.

## Tech Stack

| Layer | Technology |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.5 |
| Database | PostgreSQL 15 |
| Cache / Session | Redis 7 |
| Auth | Spring Security + JWT (JJWT) + Argon2id |
| Rate Limiting | Bucket4j Core |
| Migration | Flyway (disabled by default, `ddl-auto: update` used) |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Build | Gradle 9.4.1 |
| Infra | Docker & Docker Compose |

## Quick Start

### 1. Start Infrastructure

```bash
docker-compose up -d
```

This starts PostgreSQL and Redis in the background.

### 2. Run the Application

```bash
./gradlew bootRun
```

The app starts on **http://localhost:8085**. Database tables are created automatically via `ddl-auto: update`.

### 3. Register & Login

```bash
# Register
curl.exe -s -X POST http://localhost:8085/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@cms.com","password":"Admin12345!"}'

# Login (stores JWT in HttpOnly cookie)
curl.exe -s -c cookies.txt -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@cms.com","password":"Admin12345!"}'

# Check current user
curl.exe -s -b cookies.txt http://localhost:8085/auth/me

# Logout
curl.exe -s -b cookies.txt -c cookies.txt -X POST http://localhost:8085/auth/logout
```

> **Windows PowerShell:** Use `curl.exe` instead of `curl` to avoid alias conflicts.

## API Endpoints

Full interactive documentation available at: **http://localhost:8085/swagger-ui.html**

### Authentication (`/auth`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/register` | No | Register a new user |
| POST | `/auth/login` | No | Login and receive JWT cookie |
| POST | `/auth/logout` | Yes | Invalidate session & clear cookie |
| GET | `/auth/me` | Yes | Get current user info |

### Projects (`/api/v1/projects`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/projects` | Yes | Create a new project |
| GET | `/api/v1/projects` | Yes | List active projects |

### CMS Keys (`/api/v1`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/projects/{projectId}/keys` | Yes | Add a key to a project |
| GET | `/api/v1/projects/{projectId}/keys` | Yes | List keys of a project |

### Translations (`/api/v1`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/v1/keys/{keyId}/translations` | Yes | Add or update a translation |
| POST | `/api/v1/projects/{projectId}/translations/bulk` | Yes | Bulk import translations |
| POST | `/api/v1/projects/{projectId}/import` | Yes | Import from `.properties` file |

### Content Lookup (`/api/v1/lookup`)

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/lookup/{projectCode}` | Yes | Get all localized keys as JSON |

## Authentication & Security

- **JWT** stored in **HttpOnly, Secure, SameSite=Strict** cookies
- **Argon2id** password hashing (via BouncyCastle)
- **Brute-force protection**: account locks after 5 failed attempts for 15 minutes
- **Rate limiting**: login endpoint protected by Bucket4j (token-bucket)
- **Redis-backed** token blacklist for logout invalidation

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | dev-only key | Base64-encoded secret (min 64 chars in production) |
| `COOKIE_DOMAIN` | *(empty)* | Cookie domain scope |
| `COOKIE_SECURE` | `false` | Set `true` in production (HTTPS) |

## Project Structure

```
com.cms
├── auth/filter        # JwtAuthFilter, RateLimitFilter
├── config             # SecurityConfig, RedisConfig, AuditConfig, SameSiteCookieConfig
├── controller         # REST API endpoints (Auth, Project, CmsKey, Translation, Import)
├── domain             # JPA entities (User, Project, CmsKey, Translation, AuditLog)
├── dto                # Request/Response DTOs
├── exception          # Global exception handling
├── mapper             # MapStruct mappers
├── repository         # JPA repositories
└── service            # Business logic layer
```

## Properties Import

Upload `.properties` files (e.g. `messages_en.properties`) via multipart form to `/api/v1/projects/{projectId}/import`. The service auto-creates missing keys and saves translations.
