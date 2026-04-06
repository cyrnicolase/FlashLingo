# English Flashcard - Agent Instructions

## Build Commands

```bash
# Must run before first build or when data/BeiShiGaoZhong_1.json changes
python3 scripts/convert_to_sqlite.py

# Or use Make (includes db step automatically)
make debug    # Build debug APK
make release  # Build release APK
make install  # Install to connected device

# Direct Gradle (does NOT run db conversion)
./gradlew assembleDebug
./gradlew assembleRelease
```

## Project Structure

- **Single module**: `:app` only
- **Package**: `com.english.flashcard`
- **UI**: Jetpack Compose with Material3
- **DI**: Hilt (`@HiltViewModel`, `@Inject`)
- **Database**: Room with pre-populated `words.db`
- **Network**: Retrofit + OkHttp
- **Data source**: `data/BeiShiGaoZhong_1.json` (must convert to SQLite before building)

## Architecture

- Screens live in `app/src/main/java/com/english/flashcard/ui/screens/`
- Shared UI components in `app/src/main/java/com/english/flashcard/ui/components/`
- Navigation via `NavGraph.kt` with `Screen.kt` route definitions
- Repositories in `domain/repository/` → implementations in `data/repository/`

## Notes

- No unit tests currently exist in the repo
- `make lint` runs Android lint analysis
- The pre-populated database lives at `app/src/main/assets/database/words.db`
