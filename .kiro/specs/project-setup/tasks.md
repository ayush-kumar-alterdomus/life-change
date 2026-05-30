# Implementation Plan: Project Setup

## Overview

Initial scaffolding for the Ascend platform — a Spring Boot modular monolith backend and Angular/Ionic frontend. This plan covers project initialization, folder structure, build configuration, Docker Compose for local development, and shared tooling configuration. No business logic is implemented.

## Tasks

- [x] 1. Initialize Spring Boot backend project
  - [x] 1.1 Generate Spring Boot 3.x project with Maven wrapper
    - Create root `backend/` directory with Maven wrapper (`mvnw`)
    - Use Spring Boot 3.x, Java 21, Maven
    - Include starter dependencies: spring-boot-starter-web, spring-boot-starter-data-jpa, spring-boot-starter-websocket, spring-boot-starter-validation, spring-boot-starter-actuator, spring-boot-starter-cache
    - Add dependencies: postgresql driver, spring-boot-starter-data-redis, firebase-admin SDK, lombok, mapstruct, springdoc-openapi
    - Configure `pom.xml` with proper groupId (`com.ascend`), artifactId (`ascend-backend`), and Java 21 compiler settings
    - _Requirements: Spring Boot 3.x, Java 21, Maven_

  - [x] 1.2 Create backend module package structure
    - Create base package `com.ascend` under `src/main/java`
    - Create module packages: `auth`, `quest`, `xp`, `streak`, `arc`, `league`, `guild`, `boss`, `skilltree`, `aicoach`, `notification`, `premium`, `analytics`, `admin`, `user`, `common`
    - Within each module, create sub-packages: `controller`, `service`, `repository`, `entity`, `dto`, `mapper`
    - Add `validator`, `scheduler`, `event` sub-packages where applicable (quest, streak, league, notification)
    - Create `common/config`, `common/exception`, `common/dto`, `common/util`, `common/constants` packages
    - _Requirements: Modular monolith structure, clean architecture per module_

  - [x] 1.3 Create Spring Boot application entry point and base configuration
    - Create `AscendApplication.java` with `@SpringBootApplication` annotation
    - Create `application.yml` with profiles: `dev`, `staging`, `prod`
    - Configure dev profile: PostgreSQL connection (localhost:5432/ascend_dev), Redis connection (localhost:6379), server port 8080
    - Add placeholder properties for Firebase project config
    - Configure JPA: ddl-auto=validate, show-sql=true (dev), open-in-view=false
    - Configure actuator endpoints (health, info, metrics)
    - _Requirements: Spring Boot configuration, multi-profile support_

  - [x] 1.4 Create common module base classes
    - Create `GlobalExceptionHandler.java` with `@RestControllerAdvice` (empty handler methods with TODOs)
    - Create `ApiResponse.java` generic wrapper DTO with `success`, `message`, `data` fields
    - Create `BusinessException.java` base exception class
    - Create `AppConstants.java` with API version prefix `/api/v1`
    - Create placeholder `SecurityConfig.java` in `auth/config` (permit all for now)
    - Create placeholder `RedisConfig.java` in `common/config`
    - Create placeholder `WebSocketConfig.java` in `common/config`
    - Create placeholder `CorsConfig.java` in `common/config` (allow localhost:4200 for dev)
    - _Requirements: Common infrastructure, API standards, error handling skeleton_

- [x] 2. Initialize Angular/Ionic frontend project
  - [x] 2.1 Generate Angular/Ionic project with standalone components
    - Create `frontend/` directory
    - Initialize Ionic Angular project with standalone components (no NgModules)
    - Use Angular 17+, Ionic 7+, SCSS styling
    - Configure `angular.json` for SCSS, strict mode, and build optimization
    - Add Capacitor for native mobile builds
    - _Requirements: Angular 17+, Ionic 7+, standalone components, Capacitor_

  - [x] 2.2 Create frontend feature-first folder structure
    - Create `src/core/` with subdirectories: `auth`, `interceptors`, `services`, `config`, `constants`, `utilities`, `animations`
    - Create `src/shared/` with subdirectories: `components`, `ui`, `directives`, `pipes`, `models`, `enums`, `validators`
    - Create `src/features/` with subdirectories for each feature: `auth`, `onboarding`, `dashboard`, `quests`, `arc-mode`, `leveling`, `streaks`, `skill-tree`, `guilds`, `leagues`, `boss-battle`, `analytics`, `ai-coach`, `premium`, `social`, `profile`, `settings`
    - Within each feature, create: `pages/`, `components/`, `services/`, `state/` (where applicable)
    - Create `src/state/` for global signal stores
    - Create `src/layouts/` with `tabs/` and `auth-layout/`
    - Create `src/assets/` with `icons/`, `images/`, `animations/`, `fonts/`
    - Create `src/theme/` with `variables.scss`, `global.scss`, `mixins.scss`
    - Create `src/environments/` with `environment.ts` and `environment.prod.ts`
    - _Requirements: Feature-first architecture, domain-driven frontend_

  - [x] 2.3 Configure Angular app bootstrap and routing skeleton
    - Configure `app.config.ts` with standalone providers (provideRouter, provideHttpClient, provideIonicAngular)
    - Create `app.routes.ts` with lazy-loaded route stubs for each feature module
    - Create `tabs.routes.ts` with bottom navigation: Home, Quests, Arc Mode, Social, Profile
    - Add route guards placeholders: `auth.guard.ts`, `premium.guard.ts`, `onboarding.guard.ts`
    - _Requirements: Lazy loading, tab navigation, route guards_

  - [x] 2.4 Configure frontend environment files and base services
    - Set up `environment.ts` with: `production: false`, `apiUrl: 'http://localhost:8080/api/v1'`, Firebase config placeholders
    - Set up `environment.prod.ts` with production flag and placeholder URLs
    - Create `src/core/services/api.service.ts` — base HTTP service wrapping HttpClient with API URL prefix
    - Create `src/core/interceptors/auth.interceptor.ts` — placeholder that attaches Bearer token from storage
    - Create `src/core/interceptors/error.interceptor.ts` — placeholder for centralized error handling
    - _Requirements: Environment configuration, API layer foundation_

- [x] 3. Checkpoint - Verify project initialization
  - Ensure both projects compile/build without errors
  - Backend: `./mvnw compile` should succeed
  - Frontend: `ng build` should succeed
  - Ensure all tests pass, ask the user if questions arise.

- [x] 4. Set up Docker Compose for local development
  - [x] 4.1 Create Docker Compose configuration
    - Create `docker-compose.yml` at project root
    - Add PostgreSQL 16 service: port 5432, database `ascend_dev`, user `ascend`, password `ascend_dev_pass`
    - Add Redis 7 service: port 6379
    - Add volume mounts for data persistence (`pgdata`, `redisdata`)
    - Add healthcheck for PostgreSQL (pg_isready)
    - Add healthcheck for Redis (redis-cli ping)
    - Create `.env.example` with all environment variable placeholders
    - _Requirements: PostgreSQL, Redis, local development environment_

  - [x] 4.2 Create database initialization script
    - Create `docker/postgres/init.sql` with: CREATE DATABASE if not exists, CREATE EXTENSION for uuid-ossp
    - Mount init script in Docker Compose as `/docker-entrypoint-initdb.d/init.sql`
    - _Requirements: Database initialization, UUID support_

- [x] 5. Configure build tooling and shared configuration
  - [x] 5.1 Create shared code quality configuration files
    - Create `.editorconfig` at project root (indent_size=2 for frontend, indent_size=4 for Java, charset=utf-8, trim_trailing_whitespace=true)
    - Create `frontend/.eslintrc.json` with Angular ESLint recommended rules
    - Create `frontend/.prettierrc` with: singleQuote=true, trailingComma=all, printWidth=100, semi=true
    - Create `frontend/.prettierignore` (node_modules, dist, coverage, .angular)
    - Add ESLint and Prettier dev dependencies to frontend `package.json`
    - _Requirements: Code quality, consistent formatting_

  - [x] 5.2 Create CI configuration placeholder
    - Create `.github/workflows/ci.yml` with placeholder jobs:
      - `backend-build`: checkout, setup Java 21, run `./mvnw verify`
      - `frontend-build`: checkout, setup Node 20, npm ci, ng build, ng test (headless)
    - Add trigger on push to `main` and pull requests
    - _Requirements: CI pipeline skeleton_

  - [x] 5.3 Create project-level documentation and gitignore
    - Create root `README.md` with: project description, prerequisites (Java 21, Node 20, Docker), quick start instructions (docker-compose up, backend run, frontend serve)
    - Create root `.gitignore` combining Java (target/, *.class, .idea/) and Node (node_modules/, dist/, .angular/) patterns
    - Create `backend/.gitignore` for Java/Maven specifics
    - Create `frontend/.gitignore` for Angular specifics
    - _Requirements: Developer onboarding, clean repository_

- [x] 6. Final checkpoint - Verify complete project setup
  - Verify Docker Compose starts successfully (`docker-compose up -d`)
  - Verify backend connects to PostgreSQL and Redis (application starts without errors)
  - Verify frontend builds and serves
  - Ensure all tests pass, ask the user if questions arise.

## Notes

- **RULE: Do NOT run any mvn, gradle, npm, or test commands. Only create/edit files. No build or test verification steps.**

- No business logic is implemented — only project structure and configuration scaffolding
- Backend modules contain empty packages ready for feature implementation
- Frontend features contain empty directories following the feature-first convention
- Docker Compose provides PostgreSQL and Redis for local development only
- CI configuration is a placeholder — expand with actual test/deploy steps as features are built
- Firebase configuration uses placeholder values — real credentials added during auth feature implementation
