# LifeHub Android — Starter v1.0.0

A native Android starter app built with Kotlin and Jetpack Compose.

## Requirements
- Android 12 (API 31) or newer
- JDK 17
- Android SDK Platform 35
- Gradle 8.11.1

## First-version features
- Home dashboard with current date
- Task creation
- Mark tasks complete/incomplete
- Delete tasks
- Local SQLite persistence
- Bottom navigation (Home, Tasks, Profile)
- Modern Material 3 interface

## Build locally
From the project root:
```bash
gradle assembleDebug
```
APK output:
`app/build/outputs/apk/debug/app-debug.apk`

## Build in the cloud from a phone
1. Create a GitHub repository named `LifeHub-Android`.
2. Upload the contents of this project to the repository.
3. Open the repository's **Actions** tab.
4. Select **Build LifeHub APK** and choose **Run workflow** (or push to `main`).
5. Open the completed workflow run.
6. Download the `LifeHub-debug-apk` artifact.
7. Extract it and install `app-debug.apk` on an Android 12+ device.

For public distribution, create a signed release build before sharing widely. The debug APK is for initial testing and should not be treated as the final public release.

## Package details
- Application ID: `com.lifehub.app`
- Minimum SDK: 31 (Android 12)
- Target SDK: 35
- Version: 1.0.0

## Architecture
- UI: Jetpack Compose
- State: ViewModel
- Data access: TaskStore repository-style local data layer
- Persistence: SQLite
