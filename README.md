# TaskFlow Backend

Spring Boot 3.2 REST API for TaskFlow.

## Prerequisites

- Java 21
- Maven 3.9+
- PostgreSQL 16 (or Docker)

## Setup

### 1. Database

```bash
# With Docker:
docker run -d --name taskflow-postgres \
  -e POSTGRES_DB=taskflow -e POSTGRES_USER=taskflow -e POSTGRES_PASSWORD=taskflow \
  -p 5432:5432 postgres:16-alpine

# Or use docker-compose from the project root:
docker-compose up postgres
```

### 2. Environment Variables

```bash
export DB_USER=taskflow
export DB_PASS=taskflow
export JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
```

### 3. Run

```bash
mvn spring-boot:run
# or with dev profile:
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The API runs on `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## API Endpoints

### Auth — `/api/v1/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | No | Register new account |
| POST | `/auth/login` | No | Login, returns JWT |
| GET | `/auth/me` | Yes | Get current user profile |

### Task Lists — `/api/v1/lists`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/lists` | Yes | Get all lists for user |
| POST | `/lists` | Yes | Create new list |
| GET | `/lists/{id}` | Yes | Get single list |
| PATCH | `/lists/{id}` | Yes | Update list |
| DELETE | `/lists/{id}` | Yes | Delete list + all tasks |

### Tasks — `/api/v1`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/lists/{listId}/tasks` | Yes | Get tasks in list |
| POST | `/lists/{listId}/tasks` | Yes | Create task |
| PATCH | `/tasks/{taskId}` | Yes | Update task |
| DELETE | `/tasks/{taskId}` | Yes | Delete task |
| POST | `/lists/{listId}/tasks/bulk` | Yes | Bulk create tasks |
| PATCH | `/tasks/{taskId}/complete` | Yes | Toggle complete |

### File Import — `/api/v1/lists/{listId}/import`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/import/parse` | Yes | Parse xlsx/csv/pdf/txt → preview |
| POST | `/import/confirm` | Yes | Save parsed tasks |

### Stats — `/api/v1/stats`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/stats/summary` | Yes | Dashboard summary stats |
| GET | `/stats/streak` | Yes | Streak data |

---

## Docker

From project root:
```bash
docker-compose up --build
```
