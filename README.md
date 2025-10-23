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

2. Add the Python SDK

- Go to File → Project Structure
- In the left sidebar, click SDKs (under "Platform Settings")
- Click the + button at the top
- Select Add Python SDK from disk
- For Environment, select Existing environment radio button
- Click the folder icon next to “Python path” and navigate to: …/overthinklytics/apps/django-backend/.venv/bin/python
- Click OK

4. Set the SDK for your Project

- In the left sidebar, click Project (under "Project Settings")
- In the "SDK" dropdown, select the SDK you just created: Python 3.13 virtualenv at …overthinklytics/apps/django-backend/.venv
- Click Apply

5. Enable Django Support 

- Settings -> Languages & Frameworks -> Django ... click on "enable Django support"
  - Django project root: ...overthinklytics/apps/django-backend
  - Settings: ...django_overthinglytics/settings.py

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

---

The sections below include additional Nx-specific details and notes retained from the original project scaffold for reference.

## Finish your CI setup

[Click here to finish setting up your workspace!](https://cloud.nx.app/connect/GEIzGScIG2)


## Run tasks

To run the dev server for your app, use:

```sh
npx nx dev @overthinklytics/overthinklytics
```

To create a production bundle:

```sh
npx nx build @overthinklytics/overthinklytics
```

To see all available targets to run for a project, run:

```sh
npx nx show project overthinklytics
```

These targets are either [inferred automatically](https://nx.dev/concepts/inferred-tasks?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects) or defined in the `project.json` or `package.json` files.

[More about running tasks in the docs &raquo;](https://nx.dev/features/run-tasks?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)

## Add new projects

While you could add new projects to your workspace manually, you might want to leverage [Nx plugins](https://nx.dev/concepts/nx-plugins?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects) and their [code generation](https://nx.dev/features/generate-code?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects) feature.

Use the plugin's generator to create new projects.

To generate a new application, use:

```sh
npx nx g @nx/next:app demo
```

To generate a new library, use:

```sh
npx nx g @nx/react:lib mylib
```

You can use `npx nx list` to get a list of installed plugins. Then, run `npx nx list <plugin-name>` to learn about more specific capabilities of a particular plugin. Alternatively, [install Nx Console](https://nx.dev/getting-started/editor-setup?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects) to browse plugins and generators in your IDE.

[Learn more about Nx plugins &raquo;](https://nx.dev/concepts/nx-plugins?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects) | [Browse the plugin registry &raquo;](https://nx.dev/plugin-registry?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)


[Learn more about Nx on CI](https://nx.dev/ci/intro/ci-with-nx#ready-get-started-with-your-provider?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)

## Install Nx Console

Nx Console is an editor extension that enriches your developer experience. It lets you run tasks, generate code, and improves code autocompletion in your IDE. It is available for VSCode and IntelliJ.

[Install Nx Console &raquo;](https://nx.dev/getting-started/editor-setup?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)

## Running unit tests (Vitest)

This workspace uses Vitest for unit tests in the Next.js app `overthinklytics`.

- Install deps: `pnpm install`
- Run all tests for the app: `pnpm --filter @overthinklytics/overthinklytics test`
- From the root: `pnpm test:overthinklytics`
- Watch mode UI: `pnpm --filter @overthinklytics/overthinklytics test:ui`
- Coverage: `pnpm --filter @overthinklytics/overthinklytics coverage`

Test config lives in `apps/overthinklytics/vitest.config.ts`, with a setup file at `apps/overthinklytics/test/setup.ts`.

### Environment loading (@next/env)

We load `.env`, `.env.local`, and related files using `@next/env` so values are available consistently in Next (via `next.config.js`) and in tests (via `apps/overthinklytics/test/setup.ts`).

- Install (already added to the workspace):
  ```sh
  pnpm install
  ```
- Next config calls `loadEnvConfig(__dirname)` to load env files from the app directory.
- Vitest setup calls `loadEnvConfig(path.resolve(__dirname, '..'))` to load the same envs for tests.

Notes:
- Only variables prefixed with `NEXT_PUBLIC_` are exposed to the browser at runtime; others are server-only.
- You can keep using `process.env.MY_VAR` in code; `@next/env` ensures the values are present when running locally or in tests.

## Frontend: Seamless backend switching

The Next.js app can connect to any of your backends (Django, Kotlin, or a third service) without code changes.

There are three ways to select a backend (highest priority first):

1) Explicit base URL

- Set `NEXT_PUBLIC_API_BASE_URL` to an absolute URL, e.g. `http://localhost:8000`.
- This overrides everything else.

2) Named backend (mapped to URLs)

- Set `NEXT_PUBLIC_BACKEND` to `django`, `kotlin`, or `third`.
- Optionally define per-backend URLs via:
  - `NEXT_PUBLIC_DJANGO_URL` (default `http://localhost:8000`)
  - `NEXT_PUBLIC_KOTLIN_URL` (default `http://localhost:8080`)
  - `NEXT_PUBLIC_THIRD_URL` (default `http://localhost:3001`)

3) Runtime override (no rebuild)

- URL query: append `?backend=django` (or `kotlin` / `third`).
- Cookie/localStorage: enable the dev switcher by setting `NEXT_PUBLIC_SHOW_BACKEND_SWITCHER=1` and use the floating selector in the app UI. It persists in a cookie and `localStorage`.

Recommended: one-click demos (no env vars)

Use the preconfigured compound Run/Debug configurations in IntelliJ IDEA/WebStorm:

- Kotlin Demo — starts the Kotlin/Spring backend and the Next.js frontend pre-wired to it
- Django Demo — starts the Django backend and the Next.js frontend pre-wired to it
- Next Demo — starts the Next.js frontend only with sensible defaults

Where to find them:
- Open Run > Run… and choose one of: “Kotlin Demo”, “Django Demo”, or “Next Demo”.
- These live in .run/*.run.xml and are checked into the repo.

Notes
- No manual environment variable setup is required for these demos.
- If you need to override behavior, see the advanced options below, but prefer the demos first.

 Notes
 
 - The helper reads from public env vars at runtime in the browser, so use the `NEXT_PUBLIC_` prefix.
 - In production, set only one of the variables above in your hosting environment. No code changes required to change targets.
 - A minimal demo is available on the Dashboard page: it shows the selected backend and can ping `/health`.

## Useful links

Learn more:

- [Learn more about this workspace setup](https://nx.dev/nx-api/next?utm_source=nx_project&amp;utm_medium=readme&amp;utm_campaign=nx_projects)
- [Learn about Nx on CI](https://nx.dev/ci/intro/ci-with-nx?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)
- [Releasing Packages with Nx release](https://nx.dev/features/manage-releases?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)
- [What are Nx plugins?](https://nx.dev/concepts/nx-plugins?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)

And join the Nx community:
- [Discord](https://go.nx.dev/community)
- [Follow us on X](https://twitter.com/nxdevtools) or [LinkedIn](https://www.linkedin.com/company/nrwl)
- [Our Youtube channel](https://www.youtube.com/@nxdevtools)
- [Our blog](https://nx.dev/blog?utm_source=nx_project&utm_medium=readme&utm_campaign=nx_projects)


### Debug information toggle (bottom-left)

A small, optional debug chip can appear on the bottom-left of every page. Clicking it reveals backend connection details and quick tools.

- Enable it:
  ```sh
  NEXT_PUBLIC_SHOW_DEBUG_TOGGLE=1 npx nx dev overthinklytics
  ```
- What you get:
  - Active backend name and resolved base URL
  - A compact `Ping /health` button to test connectivity
  - If `NEXT_PUBLIC_SHOW_BACKEND_SWITCHER=1`, the in-app backend switcher appears inside the panel
- Shortcuts: Press Alt+D to toggle, ESC to close.
- Production safety: Hidden by default unless you enable `NEXT_PUBLIC_SHOW_DEBUG_TOGGLE`.



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
