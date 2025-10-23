# Overthinklytics

Modern analytics monorepo with a Next.js frontend and multiple backends (Django/Python and Kotlin/Spring), managed with Nx. This README is a practical end-to-end guide to get you from zero to local development with a working database.

If you just want the quick path: follow the Quick start checklist, then see First-time database setup and Running the apps.

---

## Contents
- Overview and repository layout
- Prerequisites (Node + pnpm, Java SDK, Python + uv)
- Quick start checklist
- Environment variables
- First-time database setup (SQLite + Prisma)
- Running the apps (Frontend, Django backend, Kotlin backend)
- Testing
- Troubleshooting

---

## Overview and repository layout
This is an Nx workspace with:
- apps/overthinklytics — Next.js 15 frontend (React 19, Vitest tests)
- apps/django-backend — Django REST backend (Python, managed via uv)
- apps/kotlin-backend — Kotlin/Spring backend (built with Gradle)
- prisma/ — Prisma schema and seed scripts for SQLite (dev.db checked in)

Nx lets you run and test each project from the monorepo root, and share tooling/configuration.

## Prerequisites
Install the following tools before you begin. Suggested versions are known-good with this repo.

- Node.js: 20.x LTS (or newer)
  - Verify: node -v
- pnpm: 10.x (or newer)
  - Install: corepack enable || npm i -g pnpm@latest || brew install pnpm
  - Verify: pnpm -v
- Git
- OpenSSL (usually preinstalled on macOS/Linux; on Windows use Git Bash or install OpenSSL)

Backend prerequisites (optional, depending on which backend you run):
- Java SDK (JDK): 21 (Temurin/Adoptium recommended)
  - Verify: java -version shows 21.x
  - Gradle Wrapper: included (Gradle 8.14) — no global install required
- Python: 3.12+ and uv (Python manager/runner)
  - Install uv: see https://docs.astral.sh/uv/getting-started/installation/
  - Verify: uv --version
- No external database required for local development
  - SQLite dev database is included in the repo at prisma/dev.db

## Configure Python Interpreter in IntelliJ IDEA

1. Ensure the virtual environment exists

`cd apps/django-backend`
`uv sync`
`uv run manage.py test` runs the tests manually
`rm -rf .idea` to remove the `.idea` directory if present 

2. Enable Django support
- Settings -> Languages & Frameworks -> Django -> Enable Django Support
  - specify project root: apps/django-backend/
  - settings file: `django_overthinglytics/settings.py`
    and settings file: `django_overthinglytics/settings.py`
  - click Apply

3. Configure Python interpreter
- File -> Project Structure -> Project Settings/Project -> SDK -> Add SDK (Python SDK) -> Select Existing -> apps/django-backend/.venv/bin/python (Apply)

4. Update Modules 
- File -> Project Structure -> Modules
- want to have django-backend dir next to overthinkyltics dir. Likely not there so click “+” -> Import Module -> django-backend -> Create module from existing sources (next) -> if two options, unclheck one that says “java web” and `django-backend/.venv` only want Python one for `apps/django-backend`. Click Next. Please select project SDK, click “+” Add Python SDK from Disk -> Select Existing -> apps/django-backend/.venv/bin/python
- click on Django underneath django-backend and make sure Settings.py file is set

5. Run Test in Gutter
  - Try running a test at `apps/django-backend/django_backend/tests.py` via the gutter to confirm.

## Quick start checklist
1) Clone and install dependencies
   - pnpm install
2) Do not change environment variables
   - Use the preconfigured compound Run/Debug configurations instead (see below). They start the right backend and wire the frontend automatically.
3) Generate Prisma client and seed demo data (SQLite)
   - pnpm prisma:generate
   - pnpm db:seed  # idempotent; creates tables if missing and fills demo data
4) Run a demo configuration
   - In IntelliJ IDEA/PyCharm/WebStorm, pick Run > Run… > “Kotlin Demo”, “Django Demo”, or “Next Demo”

## Environment variables
Create a .env file at the repo root (or apps/overthinklytics/.env.local for frontend-only) if you want to customize frontend behavior. No DATABASE_URL is required for local dev because Prisma is configured to use SQLite at prisma/dev.db by default.

```
# Frontend: select backend by name or explicit URL
# NEXT_PUBLIC_API_BASE_URL takes precedence if set
# NEXT_PUBLIC_BACKEND can be: django | kotlin | third
# NEXT_PUBLIC_DJANGO_URL defaults to http://localhost:8000
# NEXT_PUBLIC_KOTLIN_URL defaults to http://localhost:8080
# NEXT_PUBLIC_THIRD_URL  defaults to http://localhost:3001
# Example: NEXT_PUBLIC_BACKEND=django

# Optional developer toggles
# NEXT_PUBLIC_SHOW_BACKEND_SWITCHER=1
# NEXT_PUBLIC_SHOW_DEBUG_TOGGLE=1

# Optional: override Prisma DB path (not needed by default)
# DATABASE_URL="file:./prisma/dev.db"
```

Notes
- Only variables prefixed with NEXT_PUBLIC_ are exposed to the browser.
- For tests and Next server code, env files are loaded via @next/env.

## First-time database setup (SQLite + Prisma)
No external database is required. The repository includes a SQLite database at prisma/dev.db, and Prisma is configured to use it by default. For a fresh start or to re-create demo data, run the following from the repo root:

```
pnpm install
pnpm prisma:generate
pnpm db:seed
```

Notes
- The seed script is idempotent and will create tables if they do not exist (SQLite) and populate demo data.
- If you want to reset the database, delete prisma/dev.db and re-run the commands above.
- You can optionally override the database path via DATABASE_URL, e.g. DATABASE_URL="file:./prisma/dev.db".

After seeding, you can visit http://localhost:3000/demo-insights when the frontend is running.

## Running the apps

Recommended: Compound Run/Debug configurations (no env vars)
- Kotlin Demo — starts Kotlin backend + Next.js frontend pre-wired to it
- Django Demo — starts Django backend + Next.js frontend pre-wired to it
- Next Demo — starts the Next.js frontend only

Open Run > Run… and choose one of the above. These configurations are stored in .run/*.run.xml and are checked into the repo.

Advanced: CLI commands (optional)

Frontend (Next.js)
- Dev: npx nx dev overthinklytics
- Build: npx nx build overthinklytics
- Select backend at runtime (if you really need to override the demos):
  - Set NEXT_PUBLIC_BACKEND=django|kotlin, or
  - Set NEXT_PUBLIC_API_BASE_URL=http://localhost:8000 (or 8080), or
  - Append ?backend=django to the URL, and/or enable the in-app switcher with NEXT_PUBLIC_SHOW_BACKEND_SWITCHER=1

Django backend (Python)
- Prereq: uv installed
- Commands (from apps/django-backend):
  - uv sync
  - uv run manage.py migrate  # if using Django models/migrations
  - uv run manage.py runserver
- If not using the Demo configs, open http://localhost:3000/dashboard?backend=django
- Admin (if configured): http://localhost:8000/admin (default demo: admin/admin)

Kotlin backend (Spring)
- Prereq: JDK 21 installed and on PATH
- Common commands (from repo root):
  - Build tests: ./gradlew :apps:kotlin-backend:test
  - Build app:  ./gradlew :apps:kotlin-backend:build
  - Run (if bootRun is available): ./gradlew :apps:kotlin-backend:bootRun
  - Otherwise, run the built jar (adjust version if needed):
    - java -jar apps/kotlin-backend/build/libs/kotlin-backend-0.0.1-SNAPSHOT.jar
- Default port (typical Spring): 8080 — so the frontend URL mapping uses http://localhost:8080

## Testing
- Frontend unit tests (Vitest):
  - From root: pnpm test:overthinklytics
  - From app: pnpm --filter @overthinklytics/overthinklytics test
  - UI mode: pnpm --filter @overthinklytics/overthinklytics test:ui
  - Coverage: pnpm --filter @overthinklytics/overthinklytics coverage
- Django tests:
  - cd apps/django-backend && uv run manage.py test
- Kotlin tests:
  - npx nx run kotlin-backend:test
  - or: ./gradlew :apps:kotlin-backend:test

## Troubleshooting
- Prisma client not found
  - Run pnpm prisma:generate after pnpm install
- SQLite database issues
  - If you see file permission or locking errors, close running apps and retry. SQLite is a single-file DB at prisma/dev.db.
  - To reset data, delete prisma/dev.db and run pnpm db:seed again.
  - If you override DATABASE_URL, ensure it points to a valid SQLite path (e.g., file:./prisma/dev.db).
- Port conflicts
  - Frontend uses 3000, Django 8000, Kotlin 8080 — adjust the Run/Debug configurations (or env vars if using advanced overrides)
- Java version
  - Ensure java -version reports 21.x; mismatched JDKs can cause Gradle build issues
- Python/uv
  - If uv isn’t found, re-open your terminal or follow the official install docs

## Prisma setup for demo data

The project includes a Prisma schema at `prisma/schema.prisma` (SQLite provider) used by the Next.js API routes under `apps/overthinklytics/src/app/api`.

Quick start (SQLite, no external DB needed):

- Install deps:
  ```sh
  pnpm install
  ```
- Generate the client:
  ```sh
  pnpm prisma:generate
  ```
- Seed demo data (creates tables on SQLite if missing and fills demo content):
  ```sh
  pnpm db:seed
  ```

Notes:
- The monorepo points Prisma to `prisma/schema.prisma` via the `"prisma.schema"` field in the root `package.json`, so commands work from the repo root.
- Default SQLite path is `prisma/dev.db`. To reset, delete the file and run the steps above again.
- Once seeded, open `http://localhost:3000/` while running `npx nx dev @overthinklytics/overthinklytics`.

## Django Configuration

Requires `uv` be installed via [global one-liner](https://docs.astral.sh/uv/getting-started/installation/).

### Quick start:

1. Start the frontend dev server: `npx nx dev @overthinklytics/overthinklytics`

2. In a second Terminal window, switch to the Django repo: `cd apps/django-backend`
  - Sync the project's dependencies with the environment: `uv sync`
  - Run the local server: `uv run manage.py runserver`
  - Visit `http://localhost:3000/dashboard?backend=django` to see the demo data.

3. Optional: use the Django admin to visualize the data.
  - Visit `http://localhost:8000/admin` and login with `admin` / `admin`.
  - All 5 models are visible and can be explored.

4. Run Tests: 
  - Make sure in `apps/django-backend` directory and then `uv run manage.py test`.
