# WILBED AI

A Python/Kivy personal assistant prototype.

## Current features

- Futuristic Kivy chat interface with an animated AI orb.
- Offline replies for greetings, help, the current time, and today's date.
- A separate `assistant.py` module for Wilbed's local command logic.

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
2. Voice input and spoken responses.
3. Android integrations with user-granted permissions.
4. Optional online AI connection.
