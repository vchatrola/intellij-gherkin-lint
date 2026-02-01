# GitHub Issue List (GherkinLint)

GitHub‑ready issues grouped by milestone and labeled by type.

Milestones:
- **M1: Stabilize (Weeks 1–2)** — correctness and production blockers.
- **M2: Refactor & Quality (Weeks 3–6)** — architecture, maintainability, and UX.
- **M3: Release Readiness (Weeks 7–10)** — testing, tooling, docs, Marketplace polish.

---

## M1: Stabilize (Weeks 1–2)

### [bug] Fix missing pluginUntilBuild property
**Priority:** P0

**Problem**
`build.gradle.kts` references `pluginUntilBuild` but it is missing from `gradle.properties`, which can break `patchPluginXml` and publishing.

**Acceptance Criteria**
- `pluginUntilBuild` is defined in `gradle.properties`.
- `./gradlew patchPluginXml` runs successfully.

---

### [bug] Fix keyword detection for tags and "*" steps
**Priority:** P0

**Problem**
`PluginUtils.getFirstWordOnlyAlphabets()` strips `@` and `*`, so tag lines and asterisk steps never validate.

**Acceptance Criteria**
- Tag lines starting with `@` and `*` steps are correctly recognized as valid keywords.
- Add unit tests covering `@tag` and `*` cases.

---

### [bug] Harden Gemini response parsing
**Priority:** P0

**Problem**
`GherkinOutputParser.extractJsonContent()` can return null; missing fields cause `NullPointerException`.

**Acceptance Criteria**
- Parser handles null/invalid JSON gracefully with a user‑facing error.
- Missing fields default safely with clear error messaging.
- Unit tests cover malformed and partial responses.

---

### [refactor] Fix Spring context lifecycle misuse
**Priority:** P1

**Problem**
`GherkinLintServiceImpl` closes the Spring context immediately after fetching a bean, risking invalid resources and proxies.

**Acceptance Criteria**
- Context lifecycle is managed correctly or Spring is removed.
- No runtime warnings/errors due to closed context.

---

### [refactor] Remove System.out.println and noisy DEBUG logging
**Priority:** P1

**Problem**
`GeminiService` uses `System.out.println`, and global logging is set to DEBUG.

**Acceptance Criteria**
- Remove `System.out` usage.
- Adjust default logging to INFO or WARN for production.

---

## M2: Refactor & Quality (Weeks 3–6)

### [refactor] Replace JOptionPane with IntelliJ Notifications
**Priority:** P1

**Problem**
UI errors use `JOptionPane` instead of IntelliJ’s notification framework.

**Acceptance Criteria**
- All user‑facing error/success messages use IntelliJ Notifications.
- Dialogs follow IntelliJ UI conventions.

---

### [refactor] Replace Spring Boot with lightweight HTTP client
**Priority:** P1

**Problem**
Spring Boot is heavy for a plugin and can cause classloader conflicts.

**Acceptance Criteria**
- Use `java.net.http.HttpClient` or OkHttp.
- Remove Spring Boot plugin and dependency management from Gradle.
- All Gemini calls go through a dedicated `GeminiClient` interface.

---

### [refactor] Extract core logic into a separate module
**Priority:** P1

**Problem**
Core logic is tightly coupled to IntelliJ APIs, making tests difficult.

**Acceptance Criteria**
- New `core` module contains prompt building, config, parsing, and Gemini request models.
- Plugin module depends on core module only.

---

### [feature] Add configuration caching and reload on settings change
**Priority:** P2

**Problem**
Rules are loaded from disk each run; no caching and no clear reload behavior.

**Acceptance Criteria**
- Cache configuration in memory.
- Reload when settings are saved or file changes detected.

---

### [feature] Implement request timeout, retry/backoff
**Priority:** P2

**Acceptance Criteria**
- Gemini requests enforce timeouts.
- Retry logic for transient failures.
- User receives clear error on repeated failures.

---

### [feature] Add prompt size control and token budgeting
**Priority:** P2

**Acceptance Criteria**
- Prompt builder can trim examples or sections based on size.
- Optional token count guard before requests.

---

### [feature] Improve tool window UI creation
**Priority:** P2

**Problem**
`ToolWindowFactory` bypasses `ContentFactory` and mixes layouts.

**Acceptance Criteria**
- Tool window content created via `ContentFactory`.
- Consistent empty state UI and layout.

---

### [feature] Move hard‑coded model name into settings
**Priority:** P2

**Acceptance Criteria**
- Gemini model configurable via settings or `application.properties`.
- Safe default model with validation.

---

### [feature] Improve configuration merging behavior
**Priority:** P2

**Problem**
Custom arrays replace defaults entirely; new keys ignored.

**Acceptance Criteria**
- Define merge strategy (replace vs merge) per section.
- Document behavior in README.

---

### [bug] Add safe file path handling
**Priority:** P2

**Acceptance Criteria**
- Use `Paths.get(...)` or `Path.resolve(...)` for file paths.
- Cross‑platform behavior validated.

---

### [refactor] Remove deprecated Prompt class or finalize migration
**Priority:** P3

**Acceptance Criteria**
- Remove `Prompts.java` if unused, or add clear migration note.
- No unused code in production.

---

### [feature] Add rate limiting / debounce for repeated validations
**Priority:** P3

**Acceptance Criteria**
- Prevent spamming Gemini API on rapid selections.
- User gets clear feedback when throttled.

---

## M3: Release Readiness (Weeks 7–10)

### [feature] Add unit tests for config merge, prompt builder, parser
**Priority:** P1

**Acceptance Criteria**
- Unit tests for `ConfigurationMerger`, `ConfigurationLoader`, `PromptBuilder`, and `GherkinOutputParser`.
- Coverage for edge cases and invalid inputs.

---

### [feature] Add integration tests for Gemini client
**Priority:** P2

**Acceptance Criteria**
- Mocked Gemini responses validate request/response handling.
- Failure cases (timeouts, 4xx/5xx) covered.

---

### [feature] Add CI pipeline with build + tests + plugin verifier
**Priority:** P1

**Acceptance Criteria**
- GitHub Actions workflow runs `build`, tests, and Plugin Verifier.
- Qodana optional gate.

---

### [feature] Add code formatting and linting (Spotless/Checkstyle)
**Priority:** P2

**Acceptance Criteria**
- Enforced formatting in CI.
- Local `./gradlew format` or `./gradlew check` support.

---

### [docs] Add IntelliJ Notification + Privacy Notice
**Priority:** P2

**Acceptance Criteria**
- Inform users that selected text is sent to Gemini.
- Add a privacy note in settings or tool window.

---

### [docs] Refresh README and add developer guide
**Priority:** P1

**Acceptance Criteria**
- Clear setup, run, test, and publish steps.
- Add screenshots/GIFs.
- Add contribution guidelines and license.

---

### [docs] Update Marketplace metadata and changelog
**Priority:** P1

**Acceptance Criteria**
- Accurate vendor info, website, issue tracker.
- Maintain proper changelog entries per version.
- Verify sinceBuild/untilBuild for compatibility.

---

### [feature] Add unit tests for keyword detection logic
**Priority:** P2

**Acceptance Criteria**
- Tests cover `@tag`, `*` steps, and standard keywords.

