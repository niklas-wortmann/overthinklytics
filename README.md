# Overthinklytics

<a alt="Nx logo" href="https://nx.dev" target="_blank" rel="noreferrer"><img src="https://raw.githubusercontent.com/nrwl/nx/master/images/nx-logo.png" width="45"></a>

✨ Your new, shiny [Nx workspace](https://nx.dev) is almost ready ✨.

[Learn more about this workspace setup and its capabilities](https://nx.dev/nx-api/next?utm_source=nx_project&amp;utm_medium=readme&amp;utm_campaign=nx_projects) or run `npx nx graph` to visually explore what was created. Now, let's get you up to speed!

## Finish your CI setup

[Click here to finish setting up your workspace!](https://cloud.nx.app/connect/GEIzGScIG2)


## Run tasks

To run the dev server for your app, use:

```sh
npx nx dev overthinklytics
```

To create a production bundle:

```sh
npx nx build overthinklytics
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

Quick start examples

- Dev with Django backend:
  ```sh
  NEXT_PUBLIC_BACKEND=django npx nx dev overthinklytics
  ```
- Dev with explicit base URL:
  ```sh
  NEXT_PUBLIC_API_BASE_URL=http://localhost:8080 npx nx dev overthinklytics
  ```
- Show the in-app switcher (dev only):
  ```sh
  NEXT_PUBLIC_SHOW_BACKEND_SWITCHER=1 npx nx dev overthinklytics
  ```

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

The project includes a Prisma schema at `prisma/schema.prisma` used by the Next.js API routes under `apps/overthinklytics/src/app/api`.

Quick start:

- Ensure a Postgres database and set `DATABASE_URL` in `apps/overthinklytics/.env.local` or project root `.env`.
- Install deps:
  ```sh
  pnpm install
  ```
- Generate the client:
  ```sh
  pnpm prisma:generate
  ```
- Create and apply migrations locally:
  ```sh
  pnpm prisma:migrate
  ```
- Seed demo data (tenants, a demo user with id `11111111-1111-1111-1111-111111111111`, memberships, and insights):
  ```sh
  pnpm db:seed
  ```

Notes:
- The monorepo points Prisma to `prisma/schema.prisma` via the `"prisma.schema"` field in the root `package.json`, so commands work from the repo root.
- API routes expect the `X-Demo-User-Id` header to match the seeded user id when using the demo pages.
- Once seeded, open `http://localhost:3000/demo-insights` while running `npx nx dev overthinklytics`.

## Django setup for demo data

In progress commands:

Quick start:

- Switch to Django repo: `cd apps/django-backend`
- Migrate the local SQLite database: `uv run manage.py migrate`
- Run local server: `uv run manage.py runserver`
