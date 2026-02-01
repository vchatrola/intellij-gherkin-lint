# IMPROVEMENT_PLAN

This document outlines a production‑grade upgrade plan for the GherkinLint IntelliJ plugin, targeting public GitHub release, portfolio quality, and IntelliJ Marketplace readiness.

## 1) High‑Priority Findings (Bugs / Correctness Risks)

- **Missing Gradle property**: `pluginUntilBuild` is referenced in `build.gradle.kts` but not defined in `gradle.properties`, which can break `patchPluginXml` and publishing.
- **Keyword detection bug**: `getFirstWordOnlyAlphabets()` strips `@` and `*`, so tag lines and `*` steps never validate; `Constants.GHERKIN_KEYWORDS` includes them but the parser removes them.
- **Fragile JSON parsing**: `GherkinOutputParser.extractJsonContent()` can return `null`, leading to parsing exceptions; missing fields cause `NullPointerException` when `get()` is called.
- **Spring context lifecycle**: `GherkinLintServiceImpl` creates a Spring context, gets a bean, then closes the context immediately. This may invalidate resources or proxies.
- **Tool window UI creation**: `GherkinLintWindowFactory` bypasses `ContentFactory` and mixes layouts, risking tool window misbehavior and inconsistent UI rendering.
- **Logging configuration**: `logging.level.root=DEBUG` in production is noisy and may expose sensitive info; there are `System.out.println` calls in `GeminiService`.

## 2) Code Smells / Technical Debt

- **Over‑reliance on Spring Boot** inside an IntelliJ plugin; heavy footprint and potential classloader conflicts.
- **Lack of tests**: no unit, integration, or UI tests; no coverage of prompt building, parsing, or configuration merge logic.
- **Configuration merge is lossy**: custom arrays replace defaults completely; new keys in custom JSON are ignored.
- **Hard‑coded model names** (`gemini-2.5-pro`) without configuration or fallback.
- **Inconsistent error handling**: failures are reported to console but not consistently to IDE notifications.
- **Deprecated class retained** (`Prompts.java`) without clear migration or deletion.
- **File path handling** uses manual concatenation (e.g., `/`), which is not cross‑platform safe.

## 3) Refactoring & Architecture Improvements

**Goal: simplify dependencies, improve separation of concerns, and make the core logic testable.**

- **Replace Spring Boot usage** with a lightweight HTTP client:
  - Use `java.net.http.HttpClient` or OkHttp; remove Spring Boot plugin from Gradle.
  - Create a small `GeminiClient` interface + implementation to isolate the API layer.
- **Introduce a core module** (plain Java, no IntelliJ APIs) containing:
  - Prompt builders, configuration loading, merging, and output parsing.
  - This enables pure unit testing without IDE dependencies.
- **Add a service layer** in the plugin module:
  - `ValidationService` (build prompt + call AI + parse results).
  - `ConfigService` (load defaults, read custom, cache results).
- **Make parsing robust**:
  - Validate JSON response schema, handle missing fields, and surface friendly error messages.
  - Add a fallback parser for malformed responses (optional).

## 4) Modern Best Practices (Clean Code / SOLID / Error Handling)

- **Single responsibility**: separate UI rendering from validation logic.
- **Dependency injection** without Spring: use constructors and IntelliJ service injection.
- **Consistent error reporting**: use IntelliJ `Notifications` or `Messages` instead of `JOptionPane`.
- **Remove direct `System.out`**; use `Logger` only with appropriate levels.
- **Null safety**: prefer `Optional` or defensive defaults when parsing JSON.
- **Threading**: ensure network calls check `indicator.checkCanceled()` and support cancellation.

## 5) Performance & Scalability Improvements

- **Cache configuration**: load JSON rules once and only reload when settings change.
- **Prompt size control**: trim examples or use a compact prompt to reduce token usage.
- **Network timeouts and retries**: define sensible timeouts and retry/backoff for transient failures.
- **Async safety**: avoid UI operations in background threads; use `invokeLater` only for UI updates.

## 6) Security & Privacy Improvements

- **Secure API key storage**:
  - Use IntelliJ’s Password Safe or secure settings storage.
  - Avoid logging keys or full prompts.
- **Input sanitization**: explicitly limit payload size and remove secrets from prompt content.
- **Disclosure in UI**: add a privacy notice explaining data sent to Gemini.

## 7) Testing Strategy

- **Unit tests** (core module):
  - `ConfigurationMerger`, `ConfigurationLoader`, `PromptBuilder`, `GherkinOutputParser`.
- **Integration tests**:
  - Mock Gemini API and validate request/response paths.
- **UI tests**:
  - Use IntelliJ UI Test Robot for action execution and tool window results.
- **Contract tests**:
  - Validate Gemini response schema handling with recorded fixtures.

## 8) Tooling & CI/CD Upgrades

- **Static analysis**: add Spotless or Checkstyle, and enable Qodana in CI.
- **CI pipeline**: GitHub Actions for build, tests, plugin verifier, and Qodana.
- **Versioning**: adopt SemVer with a release workflow and changelog automation.
- **Dependency updates**: use Dependabot or Renovate.

## 9) Documentation & Developer Experience

- **README refresh**:
  - Add clear setup steps, environment variable guidance, and troubleshooting.
  - Include screenshots and GIFs of tool window output.
- **Developer guide**:
  - Explain how to run IDE, how to run tests, and how to publish.
- **API key guidance**:
  - Document secure handling and how to revoke keys.

## 10) IntelliJ Marketplace Readiness

- **Metadata completeness**:
  - Ensure vendor URL, contact info, and plugin description are accurate.
  - Provide a real plugin website and issue tracker link.
- **Compatibility policy**:
  - Define `sinceBuild` and `untilBuild` and update regularly.
- **UI/UX polish**:
  - Use `ContentFactory` for tool window contents.
  - Provide consistent styling and empty‑state views.
- **Versioning & change notes**:
  - Maintain a proper changelog and release notes for each build.
- **Plugin verifier**:
  - Run JetBrains Plugin Verifier in CI to validate compatibility.

## 11) Suggested Roadmap

**Phase 1 (Stabilize – 1–2 weeks)**
- Fix keyword parsing, JSON parsing, and missing `pluginUntilBuild` property.
- Remove `System.out` usage; implement proper logging and notifications.
- Ensure tool window content creation follows IntelliJ APIs.

**Phase 2 (Refactor – 2–4 weeks)**
- Extract core logic into a separate module.
- Replace Spring Boot with a lightweight HTTP client.
- Add configuration caching and robust error handling.

**Phase 3 (Quality & Release – 4–6 weeks)**
- Add unit/integration tests and CI pipeline.
- Update documentation, screenshots, and Marketplace metadata.
- Run Plugin Verifier and publish a beta release.

---

If you want, I can turn this plan into a concrete task list with file‑level changes, or start implementing the Phase 1 fixes.
