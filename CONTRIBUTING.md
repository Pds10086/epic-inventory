# Contributing

Thanks for your interest in contributing!

## Dev setup
1. Install JDK 17+ (we test on JDK 21)
2. Install Android Studio Koala (2024.1.1) or newer
3. Set `EPIC_CLIENT_ID` in your `~/.gradle/gradle.properties`:
   ```
   EPIC_CLIENT_ID=your_client_id
   ```

## Code style
- Kotlin official style (the project uses `kotlin.code.style=official`)
- 4-space indent, no tabs
- Prefer `collectAsStateWithLifecycle` over `collectAsState`
- Use `sealed class` for UI state
- Add `OptIn(ExperimentalMaterial3Api::class)` to Composable that uses `Card(onClick=...)`

## Pull request
1. Fork & create a feature branch
2. Make changes, add unit tests
3. Run `./gradlew test lint assembleDebug`
4. Open PR, link any issue

## Security
Found a security issue? Email the maintainer (see PRIVACY.md) instead of opening a public issue.
