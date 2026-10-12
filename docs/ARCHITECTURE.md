# MVP architecture and boundaries

Native Android (Java, API 26+) and browser clients use one bilingual illustrative lesson pack. Offline tutoring is authored content selected by conservative lexical retrieval. It is not a locally running language model. Only explicit short follow-ups retain the last topic; unrelated questions reset it. This keyword method still has false matches and is not a semantic safety classifier.

The Python FastAPI service stores pseudonymous accounts, salted scrypt password hashes, SHA-256 digests of expiring random bearer tokens, user-scoped sessions, chat turns, and practice counts in SQLite. API bodies are bounded at 16 KB and authenticated chat requests are limited to 30/minute. SQLite is appropriate for a single-instance pilot; it is not a horizontally scalable shared database.

Optional server inference accepts a configured NVIDIA NIM or Hugging Face endpoint exposing `/v1/chat/completions`. Retrieval happens first. The model receives one bounded passage and the current question, not the stored chat history. `use_model` is explicit and off by default. Provider failures return a labelled sample answer. Generated explanations carry `grounded: false` and a notice; prompt instructions cannot guarantee factuality.

Android stores at most 500 practice turns in Room, with backups disabled. This is OS sandbox storage, not application-layer encryption. The bearer token exists only in app memory. Closing/recreating the app requires sign-in again. API keys remain on the server. English and Devanagari OCR models are bundled through ML Kit; selected photos are processed on the device, then editable recognised text is shown before sending. TTS uses the installed Android voice; offline availability depends on voice data. Photo OCR and TTS are not WebRTC voice chat.

Browser guest practice stores topic counts in localStorage, not full questions. The service worker caches the app shell and lesson pack, never API responses. It supports offline revisits after one successful load/install, not a first-ever offline visit. Account tokens live only in memory; closing the tab signs the user out locally. Server sessions expire after 24 hours.

## Pilot limits

- 15 sample lessons; not NCERT/board-approved content. Review lessons with teachers before student pilots.
- No MFA, password recovery, parental-consent workflow, school administration, analytics pipeline or production retention policy.
- No trained weights bundled yet, no measured 750 MB RAM claim, no on-device LLM integration, and no fabricated thermal/thread controls.
- Full duplex WebRTC voice, realtime ASR/TTS, animation and semantic retrieval are subsequent milestones. The capabilities endpoint reports unavailable features honestly.
- Put the API behind HTTPS. Do not expose debug builds or the unauthenticated registration API to a public school rollout without deployment controls, consent and abuse review.
- Account deletion cascades through active database records; operators must separately define backup retention/deletion. Local Room practice is erased separately from server accounts.
- Teacher evaluation and testing on actual 2/3 GB Android phones remain necessary. An APK build is not device validation.
