# English Flashcard - Agent Instructions

## Environment Requirements

- **Java**: JDK 21 (`export JAVA_HOME=/opt/homebrew/opt/openjdk@21`)
- **Gradle**: 8.10 (wrapper), **Kotlin**: 2.0.0, **AGP**: 8.5.2
- **Kotlin 2.0 requires `org.jetbrains.kotlin.plugin.compose`** in both `build.gradle.kts` (plugins block) and `app/build.gradle.kts`

## Build Commands

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21

# Use Make — it runs database conversion automatically
make debug    # convert db → assembleDebug
make release  # convert db → assembleRelease
make install  # install to device (requires adb)

# Direct Gradle — does NOT convert database
./gradlew assembleDebug
./gradlew assembleRelease

# Lint
make lint
```

**Re-convert DB** when `data/BeiShiGaoZhong_1.json` changes: `python3 scripts/convert_to_sqlite.py`

## Architecture

- **Single module**: `:app` only, package `com.english.flashcard`
- **UI**: Jetpack Compose + Material3, screens in `ui/screens/`, components in `ui/components/`
- **DI**: Hilt (`@HiltViewModel`, `@Inject`)
- **Domain**: `domain/repository/` (interfaces) → `data/repository/` (impls)
- **Database**: Room, pre-populated from `app/src/main/assets/database/words.db`
- **Network**: Retrofit + OkHttp
- **Word mastery logic**: `correctStreak >= 2` → `isMastered = true` (`UpdateWordAfterAnswerUseCase.kt:15`)

## Notes

- No unit tests exist
- `LearningType.Test` mode randomizes word selection and skips daily progress tracking (but still saves word-level progress like `isMastered`)
- `HomeViewModel.loadData()` uses `.first()` for daily stats; use `MeViewModel` pattern with `.collect()` if real-time updates are needed
