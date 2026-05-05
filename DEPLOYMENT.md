# Deployment Guide

## 1. Local run with Docker Compose

1. Copy `.env.example` to `.env`.
2. Set your own database password in `.env`.
3. Start the stack:

```bash
docker compose up --build
```

Services:

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Healthcheck: `http://localhost:8080/actuator/health`

The PostgreSQL container starts without seed SQL because application tables are
created by Spring Boot after the database is already running. If you need demo
data, start the stack first and then run:

```powershell
./scripts/seed-restaurant-db.ps1
```

## 2. Environment variables

Backend reads configuration from environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_PORT`
- `PORT`
- `SPRING_JPA_HIBERNATE_DDL_AUTO`
- `SPRING_JPA_SHOW_SQL`
- `SPRING_JPA_FORMAT_SQL`

Frontend build can use:

- `VITE_API_BASE_URL`
- `VITE_PROXY_TARGET`

## 3. Docker files

- `Dockerfile`: Spring Boot backend image
- `frontend/Dockerfile`: frontend image with Nginx
- `frontend/nginx/default.conf`: SPA routing and `/api` proxy
- `docker-compose.yml`: frontend + backend + PostgreSQL

## 4. Free hosting recommendation

Recommended practical split:

- Backend API: Koyeb
- PostgreSQL: Koyeb Postgres or Neon
- Frontend: Vercel / Netlify / Cloudflare Pages

Why:

- one free backend instance is enough for demos
- PostgreSQL can live outside the application container
- frontend static hosting is simpler and cheaper than serving it from the backend

## 5. GitHub secrets for CI/CD

Add these secrets in GitHub repository settings:

- `KOYEB_TOKEN`
- `KOYEB_APP_NAME`
- `KOYEB_SERVICE_NAME`
- `KOYEB_HEALTHCHECK_URL`
- `SONAR_TOKEN` (optional)
- `SONAR_ORGANIZATION` (optional)
- `SONAR_PROJECT_KEY` (optional)

## 6. How the workflow works

`.github/workflows/ci.yml` does this:

1. Builds and tests the backend with Maven.
2. Builds the frontend with Node.js.
3. Builds and publishes the backend Docker image to GHCR.
4. Redeploys the backend service on Koyeb.
5. Calls the production healthcheck URL after deployment.

## 7. First Koyeb setup

The GitHub Action assumes the Koyeb service already exists once.

Initial one-time setup:

1. Create a Koyeb app and web service.
2. Point it to the Docker image `ghcr.io/<your-user-or-org>/restaurant-backend:latest`.
3. Add environment variables for the database.
4. Expose port `8080`.
5. Confirm the service responds on `/actuator/health`.

After that, each push to `main` or `master` will trigger redeploy from GitHub Actions.
