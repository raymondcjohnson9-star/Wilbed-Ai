# ATLAS — Native Android Assistant

ATLAS is being developed as a native Android application. The user interface is built with **Kotlin and Jetpack Compose**. Kivy and Buildozer are not used to build the app UI or Android APK.

## Architecture

- **Android app:** Kotlin, Jetpack Compose, Android platform APIs.
- **Local intelligence:** Kotlin tools for deterministic tasks and a Python-based development/training workspace for model research.
- **Language model:** An independently trained, locally runnable model is a future milestone. No paid AI API is required by this foundation.
- **Memory:** Private on-device storage, with explicit user control.
- **Voice:** Android speech recognition and Text-to-Speech. Background wake-word support will be designed separately around Android's permission and foreground-service rules.

## Build on a computer

Requirements:
- Android Studio (recommended), or JDK 17 and Gradle 8.9.
- Android SDK platform 35.

Open this repository in Android Studio and run the `:app` configuration, or from the repository root run:

```bash
gradle assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Current native foundation

- Jetpack Compose Android interface.
- Text input and Android speech input.
- Android Text-to-Speech.
- Explicit local memory.
- Offline programming tutor.
- A deterministic local arithmetic engine is being added to the request router.

## Roadmap

1. Native Android foundation and safe local tools.
2. Intent routing, planning, reasoning and recommendations.
3. Dataset preparation and independent small language-model training.
4. Document reading, structured data, research and analysis.
5. User-controlled memory.
6. Wake-word detection and reliable voice interaction.
7. Permission-based Android actions such as calls, messages, reminders and app launching.
8. Security, tests, accessibility and release.

## Safety and reliability

ATLAS must never claim an action succeeded unless the platform confirms it. It must ask before sending messages, placing calls, submitting forms, making purchases, or deleting data. Tax and legal estimates must show the jurisdiction, tax year, assumptions and source of the rules used.
