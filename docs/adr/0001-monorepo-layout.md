# ADR 0001 — Single monorepo for backend, frontend and shared packages

- Status: Accepted (2026-10-18)
- Decider: Abhishek Kumar (owner)

## Context
Work had split into two diverging lines on 12 Oct 2026:
- `main`: offline Kotlin WebView app + vanilla-JS web app, encrypted vault, optional on-device LLM, no backend.
- `feature/ai-mentor-mvp`: FastAPI + SQLite backend, separate Java Android app and separate web client.

Both lines carried their own copy of the same 15-lesson pack, which had already drifted (keywords differed). The product needs one client codebase that works offline and syncs with one backend.

## Decision
One repository, `Abhiindigloo/ai-mentor`, laid out as:
- `apps/web` — the learner app (also bundled in Android)
- `apps/android` — Kotlin shell from `main`
- `services/api` — FastAPI backend from `feature/ai-mentor-mvp`
- `packages/curriculum` — single lesson-pack source (the `main` copy, a strict superset of the backend copy)
- `packages/contracts` — OpenAPI + Postman
- `training/`, `docs/`, `scripts/`, `tests/`

Generated copies (`apps/web/curriculum.json`, `apps/web/sw.js`, Android `assets/www`) are produced by `npm run sync` and committed; CI fails on drift. CI runs one job per area.

Not carried over: the Java Android client and the connected web client from the feature branch (kept in git history). Their server-connected behaviour returns through the offline-first sync milestone.

## Consequences
- Lesson, API-contract and client changes land atomically in one PR.
- One CI, one issue tracker, one delivery branch (`main`).
- Clients do not call the API until sync ships — a temporary regression versus the feature branch.
- Revisit splitting when separate teams/release cadences exist, CI time hurts, or the API must become private (likely first split: `ai-mentor-api`).
- Spring Boot is not introduced; FastAPI remains the only backend stack until transactional/payment needs justify more.
