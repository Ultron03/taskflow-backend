# TaskFlow API

Backend for TaskFlow, a task tracker with XP, streaks and file import for things like LeetCode problem lists.
The React frontend is in [taskflow-web](https://github.com/Ultron03/taskflow-web).

![TaskFlow preview](https://raw.githubusercontent.com/Ultron03/taskflow-web/main/docs/preview.gif)

## Tech

Java 21, Spring Boot 3.2, Spring Security with JWT, Spring Data JPA, PostgreSQL 16.
File parsing uses Apache POI (Excel), OpenCSV and PDFBox. API docs come from springdoc.

## Running it

### With Docker

```bash
docker compose up --build
```

This starts Postgres (exposed on host port 5433) and the API on http://localhost:8080.

### Locally

Start a database first. You can run just the Postgres service from the compose file, but it's exposed on 5433, so point the app at it:

```bash
docker compose up -d postgres
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/taskflow
./mvnw spring-boot:run
```

If you already have Postgres running on 5432 with a `taskflow` database, skip the `SPRING_DATASOURCE_URL` line.

The `dev` profile recreates the schema on every start and turns on SQL and security logging:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Swagger UI is at http://localhost:8080/swagger-ui.html.

### Configuration

| Variable                | Default                                     |
| ----------------------- | ------------------------------------------- |
| `DB_USER`               | `taskflow`                                  |
| `DB_PASS`               | `taskflow`                                  |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/taskflow` |
| `JWT_SECRET`            | dev-only key in `application.yml`           |

The default `JWT_SECRET` is only for local use. For anything public, set your own base64-encoded key of at least 256 bits, for example from `openssl rand -base64 32`.

## API

Everything except register and login needs an `Authorization: Bearer <token>` header. If the token is missing or has expired, you get a `401`.

### Auth

| Method | Path                    | Description            |
| ------ | ----------------------- | ---------------------- |
| POST   | `/api/v1/auth/register` | Create an account      |
| POST   | `/api/v1/auth/login`    | Log in, returns a JWT  |
| GET    | `/api/v1/auth/me`       | Current user           |

### Lists

| Method | Path                 | Description                 |
| ------ | -------------------- | --------------------------- |
| GET    | `/api/v1/lists`      | All lists, with task counts |
| POST   | `/api/v1/lists`      | Create a list               |
| GET    | `/api/v1/lists/{id}` | One list                    |
| PATCH  | `/api/v1/lists/{id}` | Update a list               |
| DELETE | `/api/v1/lists/{id}` | Delete a list and its tasks |

### Tasks

| Method | Path                                | Description                                     |
| ------ | ----------------------------------- | ----------------------------------------------- |
| GET    | `/api/v1/lists/{listId}/tasks`      | Tasks in a list (`?completed=` / `?priority=`)  |
| POST   | `/api/v1/lists/{listId}/tasks`      | Create a task                                   |
| POST   | `/api/v1/lists/{listId}/tasks/bulk` | Create several tasks at once                    |
| PATCH  | `/api/v1/tasks/{taskId}`            | Update a task                                   |
| PATCH  | `/api/v1/tasks/{taskId}/complete`   | Mark a task done or not done                    |
| DELETE | `/api/v1/tasks/{taskId}`            | Delete a task                                   |

### Import

| Method | Path                                    | Description                                         |
| ------ | --------------------------------------- | --------------------------------------------------- |
| POST   | `/api/v1/lists/{listId}/import/parse`   | Upload an xlsx/csv/pdf/txt file and get back a preview |
| POST   | `/api/v1/lists/{listId}/import/confirm` | Save the previewed tasks                            |

Uploads are limited to 20 MB.

### Stats

| Method | Path                    | Description                                       |
| ------ | ----------------------- | ------------------------------------------------- |
| GET    | `/api/v1/stats/summary` | Total XP, completion %, current streak, today's count |
| GET    | `/api/v1/stats/streak`  | Current and longest streak, plus active days      |

## XP

| Difficulty | XP  |
| ---------- | --- |
| Easy       | 10  |
| Concept    | 15  |
| Medium     | 25  |
| Hard       | 50  |
| Project    | 100 |

Tasks with no difficulty are worth 10. Your streak counts consecutive days with at least one completed task. If you haven't finished anything yet today, the streak still counts up to yesterday.
