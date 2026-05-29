# Ascend — Gamified Personal Development Platform

Ascend is a gamified personal development platform that transforms self-improvement into an engaging RPG-like experience. Users complete quests, earn XP, maintain streaks, join guilds, and compete in leagues — all while building real-world habits and skills.

## Tech Stack

| Layer    | Technology                          |
|----------|-------------------------------------|
| Backend  | Spring Boot 3.x, Java 21, Maven    |
| Frontend | Angular 17+, Ionic 7+, Capacitor   |
| Database | PostgreSQL 16                       |
| Cache    | Redis 7                             |
| Auth     | Firebase Authentication             |

## Prerequisites

- **Java 21** (e.g., Eclipse Temurin)
- **Node.js 20** with npm
- **Docker** and **Docker Compose**
- **Angular CLI** (`npm install -g @angular/cli`)
- **Ionic CLI** (`npm install -g @ionic/cli`)

## Quick Start

### 1. Start infrastructure services

```bash
docker-compose up -d
```

This starts PostgreSQL (port 5432) and Redis (port 6379) with persistent volumes.

### 2. Run the backend

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

The API will be available at `http://localhost:8080/api/v1`.

### 3. Run the frontend

```bash
cd frontend
npm install
ng serve
```

The app will be available at `http://localhost:4200`.

## Project Structure

```
ascend/
├── backend/              # Spring Boot modular monolith
│   └── src/main/java/com/ascend/
│       ├── auth/         # Authentication & authorization
│       ├── quest/        # Quest system
│       ├── xp/           # XP & leveling engine
│       ├── streak/       # Streak tracking
│       ├── arc/          # Arc mode (focus sessions)
│       ├── league/       # Competitive leagues
│       ├── guild/        # Guild system
│       ├── boss/         # Boss battles
│       ├── skilltree/    # Skill tree progression
│       ├── aicoach/      # AI coaching
│       ├── notification/ # Notification engine
│       ├── premium/      # Premium subscriptions
│       ├── analytics/    # Analytics & insights
│       ├── admin/        # Admin panel
│       ├── user/         # User profiles
│       └── common/       # Shared utilities & config
├── frontend/             # Angular/Ionic app
│   └── src/
│       ├── core/         # Auth, interceptors, services
│       ├── shared/       # Reusable components & pipes
│       ├── features/     # Feature modules (lazy-loaded)
│       ├── state/        # Global signal stores
│       ├── layouts/      # App layouts (tabs, auth)
│       └── theme/        # SCSS variables & mixins
├── docker/               # Docker initialization scripts
├── docker-compose.yml    # Local dev infrastructure
└── .github/workflows/    # CI pipeline
```

## Development

### Backend

```bash
cd backend
./mvnw compile          # Compile
./mvnw test             # Run tests
./mvnw verify           # Full verification (compile + test + checks)
```

### Frontend

```bash
cd frontend
ng serve                # Dev server with hot reload
ng build                # Production build
ng test                 # Run unit tests
ng lint                 # Lint check
```

## Firebase Setup (Required for Auth)

Firebase is required for authentication. Follow these steps once before running the app locally.

### 1. Create a Firebase Project

1. Go to [console.firebase.google.com](https://console.firebase.google.com)
2. Click **Add project** → name it `ascend-dev` → create
3. Go to **Authentication** → **Get started** → enable **Email/Password**, **Google**, and **Anonymous**

### 2. Frontend Firebase Config

1. In Firebase console → **Project Settings** → **General** → **Add app** → **Web**
2. Register app as `ascend-web` and copy the config object
3. Create the file `frontend/src/environments/environment.local.ts` (gitignored — never committed):

```typescript
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  firebase: {
    apiKey: 'YOUR_API_KEY',
    authDomain: 'ascend-dev.firebaseapp.com',
    projectId: 'ascend-dev',
    storageBucket: 'ascend-dev.appspot.com',
    messagingSenderId: 'YOUR_SENDER_ID',
    appId: 'YOUR_APP_ID',
  },
};
```

### 3. Backend Service Account

1. In Firebase console → **Project Settings** → **Service accounts** → **Generate new private key**
2. Rename the downloaded file to `firebase-service-account.json`
3. Place it at `backend/firebase-service-account.json` (gitignored — never committed)

### 4. Enable Firebase on Backend

Create `backend/src/main/resources/application-local.yml` (gitignored — never committed):

```yaml
firebase:
  enabled: true
  project-id: ascend-dev
  credentials-file: firebase-service-account.json
```

## Running Locally (with Firebase)

### 1. Start infrastructure

```bash
docker-compose up -d
```

### 2. Run the backend with local profile

```bash
cd backend
mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev,local
```

### 3. Run the frontend with local config

```bash
cd frontend
npm install
ng serve --configuration=local
```

> The `local` configuration replaces `environment.ts` with your `environment.local.ts` at build time.
> See `angular.json` under `build.configurations.local` for the `fileReplacements` setup.

## Environment Variables

Copy `.env.example` to `.env` and fill in the required values. See the example file for all available configuration options.

## Credentials Summary

| File | Gitignored | Purpose |
|------|-----------|---------|
| `backend/firebase-service-account.json` | ✅ Yes | Firebase Admin SDK credentials |
| `backend/src/main/resources/application-local.yml` | ✅ Yes | Local backend config overrides |
| `frontend/src/environments/environment.local.ts` | ✅ Yes | Frontend Firebase config with real keys |
| `frontend/src/environments/environment.ts` | ❌ No | Placeholder only — no real credentials |

## License

Private — All rights reserved.
