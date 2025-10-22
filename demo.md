# JetBrains Booth Demo Scenarios

This document contains two end-to-end, narrative demo scenarios you can run from this repository during a live JetBrains booth session. Each scenario is written as a story with a clear workflow and a list of “surround” features to sprinkle in while talking and navigating the IDE.

The scenarios are designed to work with the pre-configured Run/Debug configurations included in this repo (see README). They require no custom environment variables by default.

- Scenario A: AI Misalignment Debt — Generate tests with AI, then reveal subtle misalignments and fix them using the IDE’s debugging, refactoring, and test tooling.
- Scenario B: Intelligent Development Environment — Showcase JetBrains Database tools + AI integration with refactorings, quick-fixes, live templates, code vision, and multi-language navigation across the monorepo.

Tip: Keep the JetBrains AI Assistant panel open and the Database tool window docked. Toggle the Services tool window to show running processes for dramatic effect.

---

## Scenario A — AI Misalignment Debt

### Story
You shipped fast with AI scaffolding a small analytics endpoint and tests. A week later, guardrail tests expose a mismatch: the AI treated the reporting window as end-inclusive and counted internal “test” events as billable. One test passes that shouldn’t; another fails. A quick debug session confirms the off-by-one and filter gap. You extract and rename the intent (`isBillableEvent`), fix the predicate (end is exclusive; exclude test/internal), update tests, and commit—the misalignment debt is paid down.

### Choose your backend (pick one for the demo)
- In the Next.js app, use the floating Backend switcher (see `apps/overthinklytics/src/components/dev/BackendSwitcher.tsx`) to select:
  - Django (Python): base `http://localhost:8000`
  - Kotlin/Spring: base `http://localhost:8080`
  - Next.js (App Router API): base `http://localhost:3000/api`
- You can also set `?backend=django|kotlin|third` in the URL or use envs (see `apps/overthinklytics/src/lib/config.ts`).

### What we’ll touch in the repo
- Kotlin backend: `apps/kotlin-backend/src/main/kotlin/com/overthinklytics/analytics/` (e.g., `AnalyticsController.kt`, `AnalyticsService.kt`)
- Django backend (Python): `apps/django-backend/django_backend/...`
- Next.js API (optional): create or use a minimal handler under `apps/overthinklytics/src/app/api/...`
- Next.js frontend (optional glance): `apps/overthinklytics`
- Tests per tech:
  - Kotlin: `apps/kotlin-backend/src/test/kotlin/...`
  - Django: `apps/django-backend/**/tests.py` or `tests/` package; run via `uv run manage.py test`
  - Next.js: component/API tests via Vitest/Jest in `apps/overthinklytics/src/**/__tests__/**`

### Workflow (step by step)

Kotlin/Spring path
1) Open the service code
   - Navigate to `apps/kotlin-backend/src/main/kotlin/com/overthinklytics/analytics/service/AnalyticsService.kt`.
   - Use Code Vision/Structure to explain responsibilities.
2) Ask AI to generate unit tests
   - Invoke JetBrains AI Assistant on `AnalyticsService` → “Write unit tests… Kotlin + JUnit 5.”
3) Create the test file
   - Save as `apps/kotlin-backend/src/test/kotlin/com/overthinklytics/analytics/service/AnalyticsServiceTest.kt`.
4) Run and debug
   - Run tests; observe mismatch. Debug with breakpoints, Watches, Smart Step Into.
5) Fix misalignment
   - Extract `isBillableEvent`, enforce end-exclusive window and exclude `test/internal`. Update tests.
6) Re-run and commit
   - All green; commit “pay down misalignment debt”.

Python/Django path
1) Open the view/service code
   - Navigate to your analytics view/service in `apps/django-backend/django_backend/...` (e.g., a DRF view or service function handling event aggregation).
2) Ask AI to generate unit tests
   - With the target function/class selected, ask AI: “Write concise Django unit tests (pytest/unittest) for empty input, off-by-one window, and category filters.”
3) Create the test file
   - Save as `apps/django-backend/django_backend/tests/test_analytics.py` (or within an existing app’s `tests.py`).
4) Run and debug
   - Terminal: `cd apps/django-backend && uv run python manage.py test`.
   - Use the IDE test runner and Debug to step through; set breakpoints in the view/service.
5) Fix misalignment
   - Extract a helper (e.g., `is_billable_event`) in Python; enforce end-exclusive window and exclude `test/internal`.
6) Re-run and commit
   - Tests pass; commit the fix with a clear message.

Next.js API path
1) Create or open a minimal API route
   - Under `apps/overthinklytics/src/app/api/analytics/route.ts` (App Router) implement a handler that aggregates events.
2) Ask AI to generate tests
   - Ask AI: “Write Vitest tests for the API handler covering empty input, end-exclusive window, and excluding `test/internal` events.”
3) Place tests
   - Save under `apps/overthinklytics/src/app/api/analytics/route.test.ts` (or `__tests__/analytics.test.ts`).
4) Run and debug
   - Run the Next.js app (`pnpm nx run overthinklytics:serve` or existing Run config). Use Debug with breakpoints in the API handler; run Vitest/Jest tests from the IDE.
5) Fix misalignment
   - Extract `isBillableEvent` utility in TS; ensure window end is exclusive and categories filtered.
6) Re-run and commit
   - Tests pass; commit with message about paying down misalignment debt.

Optional frontend check (any backend)
- Start the chosen backend + Next.js, open `http://localhost:3000/demo-insights`, and explain how back-end rules affect the UI.

### Surround scenarios/features to sprinkle in
- AI Assistant
  - Summarize the diff of your changes: “Explain what changed and why.”
  - Ask it to propose property-based tests and contrast with the classic tests it generated.
- Testing ergonomics
  - Gutter run icons, parameterized test templates, live templates for test data builders.
  - Code coverage highlighting and the Coverage tool window.
- Debugger superpowers
  - Evaluate Expression, Watches, Inline values, Smart Step Into, Method breakpoints.
  - “Drop frame” to re-run without restarting.
- Code correctness aids
  - Intentions and quick-fixes for nullability and data classes.
  - Structural Search & Replace (SSR) to fix a pattern across files.
- VCS integration
  - Local history to show you can recover pre-AI edits.
  - Shelves or partial commits to review only the misaligned chunk.

---

## Scenario B — Intelligent Development Environment (IDE) with DB Plugin + AI

### Story
Support asks for clearer insights by device and OS. In one JetBrains environment, you add `device_id` and `os` to the analytics table via the Database tool, validate, and apply. You thread those fields through Kotlin services/DTOs (default `os = "unknown"` when missing) and accept quick refactorings. In the Next.js UI, you pass optional device/OS filters and let TypeScript quick‑fixes propagate types. You run end‑to‑end and use AI to summarize risks (null handling, defaults, indexing on `os, device_id`).

### Choose your backend (pick one for the demo)
- Use the Backend switcher in the Next.js UI (or `?backend=django|kotlin|third`) to target:
  - Django (Python): `http://localhost:8000`
  - Kotlin/Spring: `http://localhost:8080`
  - Next.js API: `http://localhost:3000/api`

### What we’ll touch in the repo
- Database: Prisma/SQLite demo DB at `prisma/dev.db` (seeded via scripts in README)
- Kotlin backend: `apps/kotlin-backend/...`
- Django backend: `apps/django-backend/django_backend/...`
- Next.js API (optional): `apps/overthinklytics/src/app/api/...`
- Next.js frontend: `apps/overthinklytics/...`
- Run configurations: “Kotlin Demo”, “Next Demo” (or per‑app Nx scripts)

### Workflow (step by step)
1) Explore the current schema and data (common)
   - Open the Database tool window; connect to `prisma/dev.db`.
   - Browse tables and DDL. Ask AI: “Suggest how to add `device_id` and `os` to the analytics/events model.”

Then follow one of the backend paths:

Kotlin/Spring path
2) Plan and apply the schema change
   - In the DB console, draft and apply:
     - `ALTER TABLE events ADD COLUMN device_id TEXT;`
     - `ALTER TABLE events ADD COLUMN os TEXT;`
   - Refresh DB tree; inspect sample rows.
3) Propagate through Kotlin backend
   - Update DTOs and queries to include optional `deviceId` and `os` (use AI Assistant to draft).
   - Refactorings: Rename params; Introduce `DEFAULT_OS = "unknown"`.
   - Add small unit tests for filtering/grouping by device/OS under `apps/kotlin-backend/src/test/...`.
4) Wire into Next.js frontend
   - Add optional `deviceId` and `os` to client calls; accept TS quick‑fixes to propagate types.
5) Run end‑to‑end
   - Use “Kotlin Demo” to start backend + frontend. Visit `http://localhost:3000/demo-insights`.
6) Review
   - Run tests; Inspect Code; ask AI for risk summary (null/defaults, indexing).

Python/Django path
2) Plan and apply the schema change
   - Apply the same SQL in the DB console to `events` (or adjust to your Django model/table name).
3) Propagate through Django backend
   - Update serializers/views/services to accept optional `device_id` and `os`.
   - Default `os` to `"unknown"` if missing; surface filters in the endpoint.
   - Add unit tests in `apps/django-backend/django_backend/tests/test_analytics.py`.
   - Run with `cd apps/django-backend && uv run python manage.py test`.
4) Wire into Next.js frontend
   - Ensure the frontend calls include optional `deviceId`/`os` when Django is selected.
5) Run end‑to‑end
   - Start Django (`uv run manage.py runserver`) + Next.js (“Next Demo”). Visit `/demo-insights`.
6) Review
   - Use AI Assistant to summarize changes and potential risks.

Next.js API path
2) Plan and apply the schema change
   - Apply the same SQL in the DB console; Next.js API will read these fields directly.
3) Implement/adjust API route
   - In `apps/overthinklytics/src/app/api/analytics/route.ts`, include `device_id` and `os` in queries/aggregation; default `os` to `"unknown"`.
   - Add Vitest/Jest tests for device/OS filters.
4) Wire into Next.js frontend
   - Since backend = Next.js, client calls hit `/api/...`; add optional filters and propagate TS types.
5) Run end‑to‑end
   - Use “Next Demo” to serve the app; open `/demo-insights` and try device/OS filters.
6) Review
   - Ask AI to propose indexes (e.g., on `(os, device_id)`) and edge cases.

### Surround scenarios/features to sprinkle in
- Database plugin
  - Diff DDL before/after. Generate SQL script from model changes. Diagram view of tables.
  - Data editor conveniences: inline edits, filter rows, export to CSV.
- Cross-language navigation
  - From SQL/Prisma to Kotlin/Django usages; from Kotlin/Django to TS types; Search Everywhere/Recent Files.
- Refactorings and quick-fixes
  - Rename, Change Signature, Inline/Extract, Convert parameter to named argument.
  - Intentions like “Add missing enum entries” or “Create function from usage.”
- TypeScript/React ergonomics
  - Propagating types via quick-fixes; Component extraction.
  - ESLint + Prettier integration; Organize imports.
- AI Assistant value-adds
  - Generate migration script explanations for code review.
  - Propose test cases and edge conditions for device/OS logic.
- Performance and profiling (bonus)
  - Use HTTP client scratch file to hit backend endpoints.
  - Built-in profiler or HTTP timings view to compare responses.

---

## Practical tips for the live demo
- Use the repo’s Quick Start (see README: prisma generate/seed) before the booth opens.
- Keep one failing test prepared (rename it with a 🚨 emoji or comment) to quickly trigger the misalignment moment.
- Pin the Database console and AI Assistant tool windows.
- Practice the “one-sentence takeaway” per step so you can bail out at any time and still land the message.
- Have the compound Run configurations ready: “Kotlin Demo” and “Next Demo.”

## One-sentence takeaways
- Scenario A: AI can accelerate, but it also introduces misalignment debt—JetBrains makes that debt visible, measurable, and cheap to pay down.
- Scenario B: The IDE is your intelligent development environment—database to backend to frontend—with AI as a helpful co-pilot, not a replacement for judgment.
