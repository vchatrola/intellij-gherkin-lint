<!-- Plugin description -->
Write consistent, review-ready BDD Gherkin with a single click. GherkinLint validates selected steps in feature files and story files (Cucumber, JBehave), flags syntax/style issues, and suggests fixes so scenarios stay uniform across teams. Powered by Gemini AI for fast, reliable Gherkin linting and BDD validation.

## Key Features
- In-editor validation for Gherkin steps (Scenario/Given/When/Then/And/But) in feature/story files
- BDD style linting for Cucumber and JBehave with actionable fixes
- Gemini AI-powered suggestions for consistency and clarity
- Optional custom rules file for team standards
- Gemini model selection with cached model list to reduce API calls
- Results shown in a dedicated tool window

## Requirements
- A Gemini API key
- Internet access (Gemini API calls)
- IntelliJ-based IDE (2022.3+; 2025.3 recommended)

## Quick Start
1. Open **Settings | Tools | GherkinLint** and add your Gemini API key.
2. (Optional) Load models and choose a preferred model.
3. Select Gherkin text in a `.feature` or `.story` file.
4. Right-click -> **Validate Gherkin**.
5. View results in the **GherkinLint** tool window.

## API Key Handling
- Stored securely in the IDE Password Safe.
- If not set, the plugin can read `GOOGLE_API_KEY` from your environment.
- If both are set, the IDE-stored key is used first.
- You can clear the stored key at any time from settings.

## Get a Gemini API key
Create a free key in Google AI Studio:
```
https://aistudio.google.com/api-keys
```

## Model Usage and Cost
- Listing models and each validation call consume Gemini API quota.
- The model list is cached; use **Load / Refresh Models** only when needed.
- Re-running validation consumes additional quota.

## Custom Rules
Enable **Custom rules** and provide a JSON file to apply your team's validation standards. Use **Copy Sample** to generate a template, edit it, and select it above. Avoid adding unsupported keys. If enabled without a file, the plugin will warn and fall back to default rules. Custom rules are included in Gemini requests-avoid sensitive content.

## Privacy
Do not submit sensitive data. Selected text (and custom rules, if enabled) are sent to the Gemini API for validation.

## Support
For support or bug reports, please raise a GitHub issue.
<!-- Plugin description end -->

## Screenshots
![Selection](docs/assets/selection.png)
![Tool window output](docs/assets/tool-window.png)
![Settings](docs/assets/settings.png)

## Development
See `docs/DEVELOPER_GUIDE.md` for setup, run, test, formatting, verifier, and publish steps.

## Contributing
See `CONTRIBUTING.md` for workflow and expectations.

## License
Licensed under the MIT License. See `LICENSE`.
