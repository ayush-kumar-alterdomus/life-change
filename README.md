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

## Environment Variables

Copy `.env.example` to `.env` and fill in the required values. See the example file for all available configuration options.

## License

Private — All rights reserved.
