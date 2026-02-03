# Developer Guide

## Requirements
- JDK 21 (Gradle toolchain will auto-download if needed)
- IntelliJ IDEA 2025.3+ recommended

## Setup
```sh
./gradlew build
```

## Run the Plugin
```sh
./gradlew runIde
```

## Tests
- All tests:
```sh
./gradlew test
```
- Core module only:
```sh
./gradlew :core:test
```

## Formatting and Linting
- Auto-format:
```sh
./gradlew format
```
- Check formatting + Checkstyle:
```sh
./gradlew check
```

## Plugin Verifier
```sh
./gradlew runPluginVerifier
```

## Build Plugin ZIP
```sh
./gradlew buildPlugin
```
Output is in `build/distributions/`.

## Publish to Marketplace
Set env vars:
- `PUBLISH_TOKEN`
- `CERTIFICATE_CHAIN`
- `PRIVATE_KEY`
- `PRIVATE_KEY_PASSWORD`

Then run:
```sh
./gradlew publishPlugin
```

## Useful Reports
- Tests: `build/reports/tests/test/index.html`
- Checkstyle: `build/reports/checkstyle/main.html`
- Spotless: reported via `spotlessCheck`
