# MonoPlayer

MonoPlayer is a private-by-design, fully offline Android music player. It scans the on-device MediaStore and never asks for an account, network access, analytics consent, or cloud configuration.

![Screenshots placeholder](docs/screenshots/placeholder.png)

## Features

- Local MP3, M4A/AAC, FLAC, WAV, OGG and OPUS discovery through scoped MediaStore access
- Media3 / ExoPlayer playback in a `MediaSessionService`, with notification, lock-screen, headset and Bluetooth controls
- Monochrome black-first Compose interface with a reversed light theme
- Song, album, artist, playlist and live local search views
- Queue playback with shuffle, repeat, seeking, favorites, play history and play counts
- Offline Room persistence for playlist and per-track state
- Library updates when audio media changes; pull/rescan support is exposed to the view model

## Build

Install Android SDK Platform 35 and JDK 17, then run:

```sh
./gradlew assembleDebug test lint
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Architecture

The app uses a small MVVM arrangement:

- `data/media` queries MediaStore off the main thread and observes library changes.
- `data/db` is the local Room store for favorites, listening history and playlists.
- `playback` owns the Media3 session service and a process-side controller connection.
- `viewmodel` combines the library and local state into lifecycle-aware UI state.
- `ui` contains Compose screens and reusable playback/library components.

No application data leaves the device. Album-art URIs are supplied by Android's media provider and artwork is loaded lazily by Coil.

## Screenshots

Screenshots will be added after device/emulator capture.

## License

[MIT](LICENSE)
