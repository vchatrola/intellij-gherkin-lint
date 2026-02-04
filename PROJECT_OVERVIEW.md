# PROJECT_OVERVIEW

I built **GherkinLint** as an IntelliJ plugin that helps people check their BDD/Gherkin feature files for clarity, consistency, and common mistakes. In plain terms: you select some Gherkin text, run the action, and the plugin asks Gemini (Google’s language model) to review each line and return simple “Valid/Invalid” feedback with a reason and a suggested fix.

## What the plugin does (in simple words)
- Watches for selected Gherkin text in a supported file (e.g., `.feature`, `.story`, `.txt`).
- Builds a clear set of rules and examples (from JSON) to guide the AI.
- Sends the selected text to Gemini and gets structured feedback back.
- Shows the results in an IntelliJ tool window so you can fix issues right away.

## How the code is organized
The project is an IntelliJ plugin split into a **core module** and a **plugin module**:

- **Plugin entry points**: action, tool window, and settings are registered in `src/main/resources/META-INF/plugin.xml`.
- **User action flow**: `GherkinLintAction` is the main entry point when the user selects text and clicks “Validate Gherkin.”
- **Core module**: prompt building, config loading/merging, parsing, and Gemini DTOs live in `core/`.
- **Gemini integration**: a lightweight HTTP client (Java `HttpClient`) handles Gemini API calls.
- **Output parsing and display**: the JSON response is parsed and printed in a readable, styled console view.
- **Settings UI**: users can manage API key, model selection, and custom rules; model lists are cached.

## End‑to‑end flow (high level)
1. User selects Gherkin text and runs **Validate Gherkin**.
2. `GherkinLintAction` validates the selection (file type, keyword, minimum length).
3. `ConfigurationManager` loads the default rules, merges user rules if enabled.
4. `PromptBuilder` creates a structured prompt with rules and examples.
5. `GeminiService` sends the prompt to Gemini using `HttpClient`.
6. `GherkinOutputParser` extracts JSON results and formats them.
7. The tool window prints the results with clear status and suggestions.

## Key design decisions and best practices
- **Clear separation of responsibilities**: settings, prompt building, API calls, and UI rendering live in their own classes.
- **Configurable rules**: default rules are in `default-rules.json`, and users can override them with a custom file.
- **Safe, structured AI output**: I require Gemini to respond in JSON so parsing is reliable.
- **IDE‑friendly UX**: results appear in a dedicated tool window with styled output.
- **Fail‑fast logging**: `GherkinLintLogger` centralizes logging across the plugin.
- **Performance safety**: config loading and prompt building run off the UI thread.

## Important files and what they do
- `src/main/resources/META-INF/plugin.xml`: registers the action, tool window, and settings panel.
- `src/main/java/com/vchatrola/plugin/action/GherkinLintAction.java`: main validation flow and UI output.
- `src/main/java/com/vchatrola/plugin/service/GherkinLintServiceImpl.java`: wires the Gemini service into the plugin.
- `src/main/java/com/vchatrola/gemini/service/GeminiService.java`: request/response wrapper for Gemini calls.
- `src/main/java/com/vchatrola/gemini/api/GeminiHttpClient.java`: HTTP client that talks to Gemini.
- `core/src/main/java/com/vchatrola/prompt/PromptBuilder.java`: builds the final prompt from JSON rules.
- `core/src/main/java/com/vchatrola/config/ConfigurationManager.java`: loads and merges default/custom rules.
- `src/main/resources/default-rules.json`: default validation rules and examples.
- `src/main/resources/gherkinlint-rules-sample.json`: sample rules file users can copy and customize.
- `src/main/java/com/vchatrola/plugin/setting/*`: settings UI, persistence, and configuration flow.
- `build.gradle.kts`: Gradle build, IntelliJ plugin config, and packaging tasks.

## What a newcomer should know first
- This is a **desktop IDE plugin**, not a web app.
- The core idea is **“selected text in, structured feedback out.”**
- Rules are **data-driven** (JSON), so behavior can change without code changes.
- The Gemini API key can be stored in IDE secure storage or provided via `GOOGLE_API_KEY`.

If you read just a few files, start with `plugin.xml`, `GherkinLintAction.java`, `PromptBuilder.java`, and `GeminiService.java`—they show the full flow from UI action to AI response.
