# Indigloo AI Mentor — MVP

A runnable pilot with a Python API, browser app, native Android app, Postman collection, and Hugging Face training pipeline. English/Hindi sample tutoring works without a model. Optional server AI can use NVIDIA NIM or an OpenAI-compatible Hugging Face endpoint.

## Run the API and browser app

From the repository root, with Python 3.12:

```bash
python -m venv .venv
source .venv/bin/activate
pip install -r backend/requirements.txt
python -m uvicorn backend.app:app --host 127.0.0.1 --port 8000
```

Open http://localhost:8000/app/ for the app and http://localhost:8000/docs for interactive API docs. Start in guest mode, or register a nickname and a password of at least 12 characters. A connected account saves questions to the server. Guest mode uses the local sample pack; its browser shell becomes offline-available after the first successful load.

Docker alternative:

```bash
cp .env.example .env
docker compose up --build -d
```

Bind to localhost and place behind your HTTPS reverse proxy for remote use. Set inference secrets only on the server; see [NVIDIA configuration](docs/NVIDIA.md).

## Android

Open `android/` in Android Studio with JDK 17 and Android SDK 35. The app uses Java source, Room local practice, bundled Latin/Devanagari OCR, TTS and optional API sign-in. It works with bundled lessons without a backend or model download.

```bash
cd android
./gradlew testDebugUnitTest assembleDebug
```

The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`. Install it on a test phone. In Account, supply your HTTPS API origin. For an Android emulator debug build, use `http://10.0.2.2:8000`. Release builds reject cleartext HTTP. No production signing key is included.

Use Photo to select an image; review recognised text before tapping Ask. Listen uses the installed language voice. Local practice is under Practice; deletion controls are under Account. Server-account deletion and local-practice deletion are separate.

## Postman

Import [collection](postman/AI-Mentor.postman_collection.json) and [local environment](postman/local.postman_environment.json). Run against localhost using a disposable test account: the final request deletes that account. The collection saves bearer tokens and session IDs automatically. It covers auth, capabilities, lessons, tutoring, unrelated-question refusal, history, progress and deletion. OpenAPI is generated from the running API at `/openapi.json`.

## Model training

[Training configuration and status](training/README.md): the 104/28-example topic-split synthetic pilot and LoRA job are prepared. Training has **not** run because the connected Hugging Face credential lacks repository-write permission required to save checkpoints. This MVP does not contain trained weights or an on-device LLM.

## Verification

```bash
pip install pytest==8.3.5
python -m pytest -q
npm ci
npm test
```

See [architecture and pilot boundaries](docs/ARCHITECTURE.md). This is not a complete curriculum or a production child-facing service. WebRTC voice is not implemented. The original single-file concept demo remains at `index.html`; the new app is `web/` served by the backend.
