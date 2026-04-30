# Restaurant Frontend

React + TypeScript + Vite frontend for the existing Spring Boot restaurant backend.

## Stack

- React
- TypeScript
- Vite
- Tailwind CSS
- shadcn/ui-style component setup
- React Router
- Axios
- Sonner

## Start

1. Install dependencies:

```bash
npm install
```

2. Create an env file from the example:

```bash
cp .env.example .env
```

3. Start the backend on `http://localhost:8080`.

4. Start the frontend:

```bash
npm run dev
```

## API notes

- Default API base URL: `/api`
- Vite dev server proxies `/api` to `VITE_PROXY_TARGET`
- CRUD services live in `src/services`
- `clients`, `orders`, `categories`, and `ingredients` currently use local filtering on top of `getAll()` because the backend controllers expose list endpoints but not explicit search endpoints
- `dishes` uses the real backend search endpoint: `/api/dish/search`

## Structure

- `src/app` router setup
- `src/components` shared and UI primitives
- `src/features` entity-specific dialogs/cards
- `src/layouts` application shell
- `src/pages` route screens
- `src/services` Axios API layer
- `src/types` shared TypeScript models
