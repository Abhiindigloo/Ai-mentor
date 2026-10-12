# AI Mentor — web and Android source

This is the reconstructed 0.1.1 Android source project. The complete learner UI, curriculum and local web logic were recovered from the saved 0.1.0 APK. Kotlin and Gradle sources were reconstructed from the earlier implementation. This is editable source, not a decompiled APK.

## Build Android

Requirements: JDK 21, Android SDK platform 35/build-tools 35, and internet access for initial Gradle/dependency downloads. Runtime learning is offline.

1. Open `android/` in Android Studio and select JDK 21 for Gradle.
2. Set `ANDROID_HOME`, or create `android/local.properties` with `sdk.dir=/absolute/path/to/Android/sdk`.
3. From this project root:

```sh
node scripts/sync-assets.mjs
cd android
./gradlew assembleDebug
```

Windows: `gradlew.bat assembleDebug`. If executable permissions were lost when extracting the ZIP, run `chmod +x android/gradlew`.

APK output: `android/app/build/outputs/apk/debug/app-debug.apk`.
Install with `adb install -r <apk-path>`. Package: `co.indigloo.mentor`; Android 8+; arm64-v8a and x86_64. A different debug signing key may require uninstalling the earlier APK first, which deletes its local data. Release signing keys are deliberately not included.

## Source map

- `android/app/src/main/java/co/indigloo/mentor/MainActivity.kt`: local WebView host, allowlisted bridge, document picker, Latin/Hindi OCR, offline TTS and local AI.
- `android/app/src/main/java/co/indigloo/mentor/Vault.kt`: AES-256-GCM encrypted state with Android Keystore and atomic file writes.
- `android/app/src/main/AndroidManifest.xml`: offline permission policy and launcher.
- `android/app/build.gradle.kts`: pinned Android dependencies and SDK configuration.
- `web/`: editable UI, tutoring, 15 bilingual starter lessons, quizzes, spaced revision, local storage, offline font and service worker.
- `scripts/sync-assets.mjs`: regenerate the offline cache and copy web files into Android assets after edits.
- `tests/core.mjs`: executable learning-logic checks (`npm test`).

## Web

Run `python3 -m http.server 8000 --directory web`, then open http://localhost:8000. Use HTTPS when hosting. Offline browser reload requires an initial successful service-worker installation. No npm packages are required for this source package.

## Native features

OCR uses bundled ML Kit Latin and Devanagari recognizers and the system image picker. The learner reviews extracted text before asking. Text-to-speech requires a matching installed offline Android voice. Optional local AI accepts a compatible Android CPU `.litertlm` model for LiteRT-LM 0.8.0. Model weights are not included. Arbitrary GGUF or browser/GPU models are not compatible. RAM/storage checks cannot guarantee any particular model will fit; validate on target phones. Default guided lessons need no model.

## Validation and limitations

Reconstructed source: bridge action/payload coverage, resource/XML structure, matching web assets and core logic were checked. This reconstructed Android project has not been compiled in the restored workspace; do not treat the earlier 0.1.0 APK as a build of this 0.1.1 source. The previous implementation did compile, and its browser acceptance flow passed before the workspace reset. OCR, Keystore, TTS and model inference need physical-device acceptance testing.

The pack is illustrative, not a full validated curriculum. AI grounding is a prompt constraint, not a factuality guarantee. No cloud backend, account system, trained model, Room database or WebRTC voice chat is included. Android state is stored using the encrypted file vault; browser state uses IndexedDB. Browser encryption does not protect against same-origin malicious JavaScript. Deletion is not a forensic secure-wipe guarantee.

Noto Sans Devanagari is included under `web/fonts/OFL.txt`. Third-party dependencies retain their own licenses.
