# Italy 2026 (Android)

Offline list of every stop from the "Italy 2026" map, grouped by layer. Each layer opens to show its places with the map icon, address, a **Waze** button (navigates to the exact pin) and a **Copy** button.

## Build
- **Android Studio:** File → Open this folder → Build → Build APK(s). The APK lands in `app/build/outputs/apk/debug/app-debug.apk`.
- **GitHub Actions:** every push to `main` builds `Italy2026.apk` and publishes it under the repo's **Releases**.

## Install on the phone
Copy the APK to the phone, open it, and allow "Install unknown apps" for the app you opened it from.

## Update the list
Regenerate `app/src/main/assets/index.html` and rebuild.
