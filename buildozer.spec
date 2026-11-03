[app]

title = Wilbed AI
package.name = wilbedai
package.domain = org.wilbedai

source.dir = .
source.include_exts = py,png,jpg,jpeg,kv,atlas
source.exclude_dirs = .git,.venv,venv,bin,__pycache__

version = 0.1.0

requirements = python3,kivy

orientation = portrait
fullscreen = 0

android.api = 35
android.minapi = 23
android.archs = arm64-v8a
android.accept_sdk_license = True

[buildozer]

log_level = 2
warn_on_root = 1
