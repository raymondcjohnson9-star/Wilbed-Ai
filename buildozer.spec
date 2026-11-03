[app]

title = Wilbed AI
package.name = wilbedai
package.domain = org.wilbedai

source.dir = .
source.include_exts = py,png,jpg,jpeg,kv,atlas
source.exclude_dirs = .git,.venv,venv,bin,__pycache__,.buildozer

version = 0.1.0

requirements = python3,kivy

orientation = portrait
fullscreen = 0

android.permissions = INTERNET, RECORD_AUDIO

android.api = 34
android.minapi = 24
android.ndk = 25b
# Build for both 64-bit and 32-bit ARM Android phones.
android.archs = arm64-v8a, armeabi-v7a
android.accept_sdk_license = True

# Use a stable python-for-android release with Python 3.11 support.
p4a.branch = v2024.01.21

[buildozer]

log_level = 2
warn_on_root = 1
