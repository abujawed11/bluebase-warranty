# Bluebase Warranty

A native Android app built with Kotlin and Jetpack Compose.

## Tech Stack

- **Language:** Kotlin
- **UI Toolkit:** Jetpack Compose (Material 3)
- **Architecture components:** AndroidX Lifecycle (ViewModel, Runtime Compose), Navigation Compose
- **Build system:** Gradle (Kotlin DSL)

## Requirements

- Android Studio (latest stable)
- JDK 11+
- Android SDK with:
  - `minSdk` 24
  - `targetSdk` / `compileSdk` 36

## Getting Started

1. Clone the repository:
   ```bash
   git clone <repo-url>
   cd Bluebasewarranty
   ```
2. Open the project in Android Studio, or build from the command line:
   ```bash
   ./gradlew assembleDebug
   ```
3. Run on an emulator or connected device:
   ```bash
   ./gradlew installDebug
   ```

## Project Structure

```
app/
  src/main/java/com/sunrack/bluebase/
    MainActivity.kt      # App entry point
    ui/theme/             # Compose theme (colors, typography, theming)
```

## License

TBD
