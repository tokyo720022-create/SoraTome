# Building Tsuzuki

## With GitHub Actions (no local setup needed)

1. Open the **Actions** tab and select **Build debug APK**.
2. Click **Run workflow** (it also runs automatically after the unzip workflow).
3. When the run finishes, download the `tsuzuki-debug-apk` artifact from the run summary.
4. Unzip it and install `app-debug.apk` on an Android 8.0+ device.

## Locally

Requirements: JDK 17 and Gradle 8.11.1 (or the Android Studio bundled Gradle).

```
gradle :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`.
