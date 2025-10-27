# JetBrains Booth Demo Scenarios

This document contains two end-to-end, narrative demo scenarios you can run from this repository during a live JetBrains booth session. Each scenario is written as a story with a clear workflow and a list of “surround” features to sprinkle in while talking and navigating the IDE.

The scenarios are designed to work with the pre-configured Run/Debug configurations included in this repo (see README). They require no custom environment variables by default.

- Scenario A: AI Misalignment Debt — Generate tests with AI, then reveal subtle misalignments and fix them using the IDE’s debugging, refactoring, and test tooling.
- Scenario B: Intelligent Development Environment — Showcase JetBrains Database tools + AI integration with refactorings, quick-fixes, live templates, code vision, and multi-language navigation across the monorepo.

Tip: Keep the JetBrains AI Assistant panel open and the Database tool window docked. Toggle the Services tool window to show running processes for dramatic effect.

---

## Scenario A — AI Misalignment Debt

### Story
You had to hit a deadline, so you were a little sloppy on writing tests. But after the release you try to make up for it by writing some tests. 
But testing isn't fun, so you decide to have them generated via AI, and while they look fine on the first glance, there are subtle errors that you need to fix.

### Workflow (step by step)

Kotlin/Spring path
1) Open the service code
   - Navigate to `apps/kotlin-backend/src/main/kotlin/com/overthinklytics/analytics/service/AnalyticsService.kt`.
   - Use Code Vision/Structure to explain responsibilities.
2) Ask AI to generate unit tests
   - Invoke JetBrains AI Assistant on `AnalyticsService` → “Write unit tests… Kotlin + JUnit 5.” (fallback with a failing test is checked in with git tag TODO add name here)
3) Create the test with AI generate test feature 
4) Checkout `scenario-1-failing-tests`
5) Run and debug `AnalyticsServiceTest`
   - Run tests; observe mismatch. Debug with breakpoints, Watches, Smart Step Into.
6) Fix misalignment
7) Re-run and commit

Python/Django path (TODO dry-run)
1) Open the view/service code
   - Navigate to your device share model in `apps/django-backend/django_backend/models.py` 
2) Ask AI to generate unit tests
   - With the target function/class selected, use generate unit test feature
3) Create the test file
   - Save as `apps/django-backend/tests/test_deve_share.py` (should be done by AIA).
4) Checkout `scenario-1-failing-tests`
5) Run and debug `apps/django-backend/tests/test_deve_share.py`
   - Use the IDE test runner and Debug to step through; set breakpoints in the test/model.
6) Fix misalignment
   - Couple failing tests in DeviceShareModelTest (wrong assertions can be confirmed with debugging tools)
7) Re-run and commit
   - Tests pass; commit the fix with a clear message.

Next.js API path
1) Open the view/service code
  - Navigate to your device share api in `apps/overthinklytics/src/app/api/analytics/device-share/route.ts`
2) Ask AI to generate unit tests
  - With the target function/class selected, use generate unit test feature
3) Create the test file
  - Save as `apps/overthinklytics/src/app/api/analytics/device-share/route.test.ts` (should be done by AIA).
4) Checkout `scenario-1-failing-tests`
5) Run and debug
  - Use the IDE test runner and Debug to step through; set breakpoints in the test/api route.
6) Fix misalignment
  - a failing tests in GET /analytics/device-share (wrong assertions can be confirmed with debugging tools)
7) Re-run and commit
  - Tests pass; commit the fix with a clear message.

### Surround scenarios/features to sprinkle in
- AI Assistant
  - Summarize the diff of your changes: “Explain what changed and why.”
  - Ask it to propose property-based tests and contrast with the classic tests it generated.
- Testing ergonomics
  - Gutter run icons, parameterized test templates, live templates for test data builders.
  - Code coverage highlighting and the Coverage tool window.
- Debugger 
  - Evaluate Expression, Watches, Inline values, Smart Step Into, Method breakpoints.
- Code correctness aids
  - Intentions and quick-fixes for nullability and data classes.
  - Structural Search & Replace (SSR) to fix a pattern across files.
- VCS integration
  - Local history to show you can recover pre-AI edits.
  - Shelves or partial commits to review only the misaligned chunk.
- Structure View
- Junie GitHub Agent (still in beta)
  - https://github.com/niklas-wortmann/overthinklytics/pull/9

---

## Scenario B — Intelligent Development Environment (IDE) with DB Plugin + AI

### Story
Support asks for clearer insights by device and OS. In one JetBrains environment, you add `os` to the device-share table via the Database tool, validate, and apply. You thread those fields through Kotlin services/DTOs (default `os = "unknown"` when missing) and accept quick refactorings. In the Next.js UI, you pass optional device/OS filters and let TypeScript quick‑fixes propagate types. You run end‑to‑end and use AI to summarize risks (null handling, defaults, indexing on `os, device_id`).

### Workflow (step by step)
1) Explore the current schema and data (common)
   - Open the Database tool window; connect to `prisma/dev.db`.
   - Browse tables and DDL. Ask AI: “write the db script to add os to the device share model ”

Then follow one of the backend paths:

Kotlin/Spring path
2) Plan and apply the schema change
   - In the DB console, draft and apply:
     - `ALTER TABLE DeviceShare ADD COLUMN os TEXT;`
   - Refresh DB tree; inspect sample rows.
3) Propagate through Kotlin backend
   - Update DTOs and queries to include `os` (apps/kotlin-backend/src/main/kotlin/com/overthinklytics/analytics/entity/DeviceShareEntity.kt)
4) Review
  - check device-share API endpoint to make sure it includes `os` in the body

Python/Django path
2) Plan and apply the schema change
  - In the DB console, draft and apply:
    - `ALTER TABLE DeviceShare ADD COLUMN os TEXT;`
  - Refresh DB tree; inspect sample rows.
3) Propagate through Django backend
   - update model (`os = models.TextField(null=True, blank=True)`)
   - update serializer (`os = serializers.CharField(allow_null=True, required=False)`)
4) Review
  - check device-share API endpoint to make sure it includes `os` in the body

Next.js API path (TODO: Dry-run)
2) Plan and apply the schema change
   - Apply the same SQL in the DB console; Update the Schema Next.js API will read these fields directly.
3) Update the `schema.prisma`
   - add `  os           String?` to the DeviceShare model.
4) Wire into Next.js api
   - add `os: r.os` to the row mapping
5) Review
   - check device-share API endpoint to make sure it includes `os` in the body

### Important
In case of emergency the solution has been pushed to the `scenario-2-complete` branch.

Remember to cleanup the DB afterwards
`ALTER TABLE DeviceShare DROP COLUMN os;`

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
