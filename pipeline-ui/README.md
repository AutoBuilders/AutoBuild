# Pipeline UI

React + TypeScript + Vite frontend for the AutoBuild pipeline service.

## Prerequisites

- Node.js 22 (see [.nvmrc](.nvmrc))
- npm

## Getting Started

```bash
npm install
```

## Running Locally

| Command | Description |
|---|---|
| `npm run local-e2e` | Vite dev server using `.env.local-e2e` (connects to real local backend + Keycloak) |
| `npm run local-with-mock` | Vite dev server with mocked API (no backend required) |
| `npm run production` | Build + preview with production settings |

## Environment Files

| File | Mode | Purpose |
|---|---|---|
| `.env.local-e2e` | `local-e2e` | Local end-to-end testing against a locally running backend and Keycloak |
| `.env.local-with-mock` | `local-with-mock` | Local development with a mocked OpenAPI server (no real backend) |

### `.env.local-e2e`

Used when running `npm run local-e2e`. Points to localhost backend and Keycloak:

```
VITE_ENV=local-e2e
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_IDP=keycloak
VITE_KC_URL=http://localhost:8180
VITE_KC_REALM=SpringBootRealm
VITE_KC_CLIENTID=pipeline-app
```

## Docker Image

The UI is packaged as a Docker image using a two-stage build (Node 22 build → `serve` runtime).

### Default Build

Builds with the production defaults baked into the [Dockerfile](Dockerfile):

```bash
docker build -t pipeline-ui .
```

### Build with `.env.local-e2e` Values

To produce an image that connects to a locally running backend and Keycloak (matching `.env.local-e2e`):

```bash
docker build \
  --build-arg VITE_API_BASE_URL=http://localhost:8080/api/v1 \
  --build-arg VITE_IDP=keycloak \
  --build-arg VITE_KC_URL=http://localhost:8180 \
  --build-arg VITE_KC_REALM=SpringBootRealm \
  --build-arg VITE_KC_CLIENTID=pipeline-app \
  -t pipeline-ui:local-e2e \
  .
```

The image exposes port `3000` and serves the built static files via `serve`.

### Build Args

| Arg | Default (Dockerfile) | Description |
|---|---|---|
| `VITE_API_BASE_URL` | `http://pipeline-app:8080/api/v1` | Backend API base URL |
| `VITE_IDP` | `keycloak` | Identity provider |
| `VITE_KC_URL` | `http://auth-service:8080` | Keycloak base URL |
| `VITE_KC_REALM` | `SpringBootRealm` | Keycloak realm |
| `VITE_KC_CLIENTID` | `pipeline-app` | Keycloak client ID |

## ESLint

For type-aware lint rules in production applications, update `eslint.config.js` to extend `tseslint.configs.recommendedTypeChecked` and set `parserOptions.project`:

```js
export default defineConfig([
  {
    files: ['**/*.{ts,tsx}'],
    extends: [tseslint.configs.recommendedTypeChecked],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
    },
  },
])
```
