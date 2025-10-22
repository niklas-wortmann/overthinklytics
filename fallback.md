# 1) Git prep: checkpointed flows you can jump to instantly
- Create lightweight tags for each scenario and step so you can `git switch` without thinking.
  - Example tags:
    - Scenario A (AI Misalignment Debt):
      - `a0_clean` (baseline, all tests passing)
      - `a1_generated_tests` (AI‑generated tests added; one failing)
      - `a2_fix_misalignment` (predicate extracted, tests green)
    - Scenario B (Intelligent Dev Env):
      - `b0_schema_before` (no `device_id`, `os` in DB)
      - `b1_schema_added` (columns added + backend threading stubbed)
      - `b2_frontend_wired` (Next.js filters/types propagated)
- Keep short branches mirroring the tags if you prefer: `demo/a0`, `demo/a1`, …
- Pre-write commit messages that tell the story (AI wrote tests; off‑by‑one; exclude test/internal; add `device_id`/`os`; default `os = "unknown"`).
- Optional: record a tiny, human‑readable `scripts/rewind.sh` that resets to a tag and restarts services.

```
#!/usr/bin/env bash
set -e
TAG=${1:?"usage: scripts/rewind.sh <tag>"}
git fetch --tags || true
git reset --hard "$TAG"
# Optionally re-seed and restart
pnpm -w prisma:seed || true
```

---

# 2) Offline-first dependency caches (flaky Wi‑Fi proof)
Prepare caches for all three stacks so install/build/test don’t need the internet.

- Node/Next.js
  - Use pnpm store (workspace root):
    - `pnpm -w install` to warm the store.
    - Export store (optional) to a USB/NAS for teammates.
  - Lock Node version with Volta or `.nvmrc` and preinstall it.
- Kotlin/Spring
  - Run once to fill Gradle/Maven caches: `./gradlew build` (or your wrapper) while online.
  - Enable Gradle offline during the booth: `./gradlew test --offline` (when applicable).
- Python/Django
  - Pre-create a `uv` cache by running:
    - `cd apps/django-backend && uv sync && uv run python manage.py check`.
  - Keep the `.venv` (or `uv` environment) on disk; do not clean it before the event.
- Database
  - Ensure `prisma generate` has run; seed data exists in `prisma/dev.db`.

Tip: Do a full dry‑run after disabling Wi‑Fi to confirm nothing hits the network (fonts, analytics, package registries, CDNs).

---

# 3) Data prep: pre-seeded DB and "demo moments"
- Seed database
  - Run the repo’s Quick Start (see README) before the booth: `pnpm -w prisma:generate && pnpm -w prisma:seed`.
  - Keep a copy of `prisma/dev.db` in `scripts/backups/dev.db.bak`. If something corrupts the DB, copy it back quickly.
- Prepare a visible failing test for Scenario A
  - Mark it clearly (emoji in name or a comment) so you can trigger the “misalignment” moment instantly.
  - Kotlin example test filename: `apps/kotlin-backend/src/test/kotlin/.../AnalyticsServiceTest.kt`.
  - Django example: `apps/django-backend/django_backend/tests/test_analytics.py`.
  - Next.js example: `apps/overthinklytics/src/app/api/analytics/route.test.ts`.

---

# 4) Start/Stop scripts and health checks
Make one‑liners for each backend and a sanity ping.

- Scripts (choose names you like; wire to IDE Run configs)
```
# Root package.json (examples)
"scripts": {
  "demo:kotlin": "nx run kotlin-backend:serve",
  "demo:next": "nx run overthinklytics:serve",
  "demo:django": "uv run python apps/django-backend/manage.py runserver 0.0.0.0:8000",
  "demo:all": "concurrently -k \"pnpm demo:kotlin\" \"pnpm demo:next\""
}
```

- Quick health checks (HTTP client scratch or curl aliases)
```
# Kotlin/Spring (8080)
curl -fsS http://localhost:8080/actuator/health || echo "kotlin backend not healthy"
# Django (8000)
curl -fsS http://localhost:8000/health || echo "django backend not healthy"
# Next.js API (3000/api)
curl -fsS http://localhost:3000/api/analytics || echo "next api not responding"
```

- Keep an IDE HTTP scratch file with these requests for a visual check.

---

# 5) Environment normalization (no surprises on stage)
- Ports: confirm `8000`, `8080`, `3000` are free. Pre-make a `scripts/killports.sh`.
```
# macOS/Linux
kill -9 $(lsof -ti:3000,8000,8080) 2>/dev/null || true
```
- Backend selection: verify the switcher and envs
  - `apps/overthinklytics/src/components/dev/BackendSwitcher.tsx` is visible in the UI.
  - `apps/overthinklytics/src/lib/config.ts` respects `?backend=django|kotlin|third` and env fallbacks.
- Node/Java/Python versions are pinned and installed (Volta/SDKMAN!/uv).
- Fonts and assets are local (no CDN fetch in airplane mode).

---

# 6) Pre-baked fallbacks (when everything goes sideways)
- Short screen recordings (30–60s) of each key moment:
  - Failing test → debug → fix → green.
  - DB change (`ALTER TABLE ... ADD COLUMN device_id/os`) → propagate → UI filter works.
- Screenshot deck for each step to narrate without a live app.
- A minimal mock server fallback (if DB/backends die):
```
# Tiny mock with Node (offline fallback)
node -e "require('http').createServer((req,res)=>{res.end(JSON.stringify({ok:true,data:[]}))}).listen(8080)"
```
- A “golden” branch with everything green you can always return to: `demo/stable`.

---

# 7) Rehearsal checklist (do this the night before and each morning)
- Install & cache
  - `pnpm -w install && pnpm -w build` (or `nx` targets)
  - `./gradlew build` (fills caches)
  - `cd apps/django-backend && uv sync && uv run python manage.py check`
- DB ready
  - `pnpm -w prisma:generate && pnpm -w prisma:seed`
- Start services
  - Kotlin + Next.js: `pnpm demo:all`
  - Or Django + Next.js: start each; verify via health checks.
- Browser sanity
  - Open `http://localhost:3000/demo-insights`.
  - Use the Backend Switcher; confirm requests hit the chosen base (`8000`, `8080`, or `3000/api`).
- Hotkeys you’ll use: open AI Assistant, run tests from gutter, toggle Services tool window.

Print or pin this list in your IDE.

---

# 8) Failure-mode playbook (what you say and do)
- Network dies mid‑demo
  - Switch to offline health checks and local screen recording; narrate what you’d have shown.
  - If Next.js HMR stalls, hard refresh or restart only the frontend; keep backend running.
- Tests won’t run
  - Switch Git tag to the prepared failing test state (`a1_generated_tests`), re-run from IDE gutter.
- DB migration fails
  - Restore `prisma/dev.db` from `scripts/backups/dev.db.bak` and continue from `b1_schema_added` tag.
- Port conflict
  - Run `scripts/killports.sh`, then re-run.

Have one-sentence takeaways ready (already in `demo.md` lines 203–206) so you can wrap early if needed.
