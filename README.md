# AI Mentor — Offline Tutor Demo

A self-contained browser demo of a textbook-grounded tutor for three school learning bands. It includes English/Hindi UI, a small built-in passage pack, a scripted offline tutor, optional LiteRT-LM Gemma 4 text generation through WebGPU, optional cloud-adapter detection, local retrieval and prompt budgeting, encrypted progress metadata, and a demo erase control.

## Run it

Open `index.html` from a local static server. For example, with Python installed:

```sh
python3 -m http.server 8000
```

Then visit `http://localhost:8000` in a modern browser. The scripted tutor works without a model download. Gemma requires a compatible browser with WebGPU, an internet connection for the initial runtime/model download, and several gigabytes of available storage. Once loaded, generation can run on-device. Browser support and model performance vary.

## Important demo limits

- The built-in passages are illustrative sample content, not a complete or curriculum-validated textbook.
- Retrieval uses local keyword overlap. The guardrail declines when no passage is sufficiently relevant, but this prototype is not a production-grade factuality system.
- The LiteRT-LM browser model path is text-only. The photo button is retained as an explicit unavailable-state placeholder; no image is sent anywhere.
- “Simulate airplane mode” disables the optional cloud adapter in the app; it does not turn off the device’s network.
- Progress metadata is encrypted with AES-GCM in the browser profile. The demo PIN is hard-coded, and browser-side encryption is not a substitute for a production security design.
- The Claude adapter is used only when a compatible `window.claude` integration is supplied by the host page.

## Repository contents

- `index.html` — complete single-file demo.
- `README.md` — run instructions and prototype limitations.
