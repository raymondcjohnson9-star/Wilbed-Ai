# ATLAS

ATLAS is an independent personal-assistant project being developed from the existing Python/Kivy Wilbed AI application.

## Project direction

ATLAS is a modular assistant, not a single chatbot. Planned components include request routing, task planning, deterministic tools, a separately trained language model, local approved memory, document processing, voice, and Android integrations.

The project is being developed without paid AI APIs. The current foundation uses local Python logic. It does not yet provide general-purpose model reasoning, online research, background wake-word detection, or phone automation.

## Run on a computer

Use Python 3.10 or newer.

```bash
python -m venv .venv
python -m pip install -r requirements.txt
python main.py
```

## Run tests

```bash
python -m unittest discover -s tests -v
```

## Build for Android

Android packaging uses Buildozer and python-for-android. The Android package naming is intentionally retained from the existing configuration while the visible app name changes to ATLAS.

```bash
python -m pip install buildozer cython==0.29.33
buildozer android debug
```

The first build downloads the Android toolchain and can take considerable time.

## Architecture

- `main.py` — Kivy user interface.
- `assistant.py` — existing offline programming-tutor fallback.
- `atlas_core/router.py` — first ATLAS request-routing layer.
- `atlas_core/math_engine.py` — safe arithmetic evaluator.
- `tests/` — automated tests for deterministic tools.
- `buildozer.spec` — Android packaging configuration.

## Development stages

1. Foundation and safe tool routing.
2. Language understanding, task planning and recommendations.
3. Dataset preparation and a small independently trained language model.
4. Document processing, research and advanced analysis.
5. Local memory and personalisation controls.
6. Voice activation and Android integrations.
7. Security, reliability, performance and release.

## Safety and reliability

ATLAS must not claim an action succeeded unless the result is confirmed. It must ask before sending messages, placing calls, submitting forms, making purchases, or taking other consequential actions. Calculations and legal or tax estimates must show their assumptions and limitations.
