# Indigloo AI Mentor

An offline-first, bilingual (English/Hindi) Socratic tutor for K-12 students on low-cost devices, with a backend for accounts, history and optional server AI. Pre-pilot: 15 illustrative lessons, no students yet.

This repository is a **monorepo**. See [ADR 0001](docs/adr/0001-monorepo-layout.md) for why.

```
apps/web/              Learner app (vanilla ES modules, PWA). Also bundled inside the Android app.
apps/android/          Android shell (Kotlin): WebView host, encrypted vault, OCR, TTS, optional on-device LLM.
services/api/          Backend (Python FastAPI + SQLite): accounts, sessions, chat, progress, optional server AI.
packages/curriculum/   Single source of the lesson pack, used by web, Android, API and training.
packages/contracts/    OpenAPI spec and Postman collection.
training/              Hugging Face LoRA pilot (prepared, not run).
scripts/               Build helpers (sync-assets).
tests/                 Repo-level checks (CI configuration).
docs/                  Architecture, NVIDIA notes, validation record, ADRs.
index.html             Original single-file concept demo.
```

## Quick start

Requirements: Node ≥ 20, Python 3.12, JDK 21 + Android SDK 35 (Android only).

```bash
npm test                 # web logic + CI config checks (no npm dependencies)
npm run sync             # after editing apps/web or packages/curriculum — commit the result
```

**Web app alone (offline guest mode):**
```bash
python3 -m http.server 8000 --directory apps/web    # http://localhost:8000
```

**Backend (also serves the web app at /app/):**
```bash
cd services/api
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt pytest==8.3.5
python -m pytest -q
python -m uvicorn mentor_api.app:app --host 127.0.0.1 --port 8000   # /app/, /docs, /openapi.json
```
Or with Docker from the repository root: `cp .env.example .env && docker compose up --build -d` (binds 127.0.0.1:8000; put an HTTPS reverse proxy in front for remote use).

Configuration (environment): `DATABASE_PATH`, `CURRICULUM_PATH`, `WEB_DIR` (defaults resolve to this repo), and optional server AI `MODEL_BASE_URL`, `MODEL_NAME`, `MODEL_API_KEY` — server-side only, never in apps or commits. See [NVIDIA notes](docs/NVIDIA.md).

**Android:**
```bash
npm run sync
cd apps/android && ./gradlew testDebugUnitTest assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```
Set `ANDROID_HOME` or `apps/android/local.properties` (`sdk.dir=...`). Package `co.indigloo.mentor`, Android 8+, arm64-v8a and x86_64. Release signing keys are not included.

**API contract tests:** import `packages/contracts/postman/` into Postman; use a disposable account — the last request deletes it.

**Training pilot:** see [training/README.md](training/README.md). `python training/prepare_data.py` regenerates data from the curriculum.

## Current limitations

- The web and Android apps run fully offline but **do not talk to the backend yet**. Offline-first sync (outbox, delta pull, persistent sign-in) is the next milestone; the earlier connected Java client remains in git history on `feature/ai-mentor-mvp` for reference.
- Retrieval is keyword-based; content is illustrative and needs teacher review.
- No parental-consent flow, age gate, MFA or password recovery — required before any pilot with children.
- Physical-device validation (OCR, TTS, memory, battery) is pending.

See [architecture and boundaries](docs/ARCHITECTURE.md).

Noto Sans Devanagari is included under `apps/web/fonts/OFL.txt`. Third-party dependencies retain their own licenses.
