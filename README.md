# WILBED AI

A Python/Kivy personal assistant prototype.

## Current features

- Futuristic Kivy chat interface with an animated AI orb.
- Offline replies for greetings, help, the current time, and today's date.
- A multi-language programming tutor for Python, Kotlin, Java, JavaScript, C++, HTML, and CSS, with short explanations, code examples, exercises, and lesson progression.
- A separate `assistant.py` module for Wilbed's local command logic.

## OpenAI backend

Wilbed keeps the OpenAI API key on a trusted server. Never place the key in
the Android app or commit it to GitHub.

1. In a Python 3.10+ environment, install the backend packages:

   ```bash
   python -m pip install -r backend/requirements.txt
   ```

2. Set `OPENAI_API_KEY` as a server environment variable.
3. Start the backend from the repository root:

   ```bash
   uvicorn backend.server:app --host 0.0.0.0 --port 8000
   ```

4. Deploy the backend behind HTTPS, then set `API_BASE_URL` in `config.py`
   to its HTTPS base URL. Rebuild the Android app.

The backend uses the OpenAI Responses API and keeps the conversation going
with the previous response ID. If `API_BASE_URL` is empty, Wilbed continues
to use its built-in offline assistant.

## Voice controls

Tap VOICE in the app footer to start Android speech recognition. Wilbed then
sends the recognized words to the configured AI backend and speaks the reply
using Android text-to-speech. Microphone access is requested when you tap
VOICE; the app does not continuously record in the background.

## Run on a computer

1. Install Python 3.10 or newer.
2. Install the dependencies:

   ```bash
   python -m pip install -r requirements.txt
   ```

3. Start Wilbed:

   ```bash
   python main.py
   ```

## Build for Android

This project is being prepared for Android packaging with Buildozer.

On a supported Linux environment, install Buildozer and its Android build dependencies, then run:

```bash
buildozer android debug
```

The first Android build can take a while because Buildozer downloads the Android toolchain.

## Project files

- `main.py` — Kivy user interface and chat interaction.
- `assistant.py` — offline assistant response logic.
- `requirements.txt` — Python dependencies.
- `buildozer.spec` — Android packaging configuration.

## Development roadmap

1. Offline assistant brain and chat connection.
2. Beginner lessons, examples, and practice tasks for Python, Kotlin, Java, JavaScript, C++, HTML, and CSS.
3. Voice input and spoken responses.
4. Android integrations with user-granted permissions.
5. Optional online AI connection.
