package com.vchatrola.prompt;

public class PromptTemplate {

    public static final String CONTEXT_TEMPLATE = """
            **CONTEXT**
            * {CONTEXT}

            **TASKS:**
            {TASKS}

            """;

    public static final String SCENARIO_TEMPLATE = """
            **SCENARIO GUIDELINES**
            **Data:** The line starts with "Scenario:".
            **Requirements:**
            1. **Syntax:**
                * The scenario title must begin with the keyword "Scenario:".
            2. **Clarity and Conciseness:**
                * The scenario title should clearly communicate the scenario's purpose without unnecessary details.
            {STRUCTURE_SECTION}
            {REQUIREMENTS_SECTION}
            **Examples:**
            {VALID_EXAMPLES_SECTION}
            {INVALID_EXAMPLES_SECTION}

            """;

    public static final String GIVEN_TEMPLATE = """
            **GIVEN STEP GUIDELINES**
            **Data:** The line starts with "Given" or the previous line started with "Given" and the current line starts with "And".
            **Requirements:**
            1. **Given statements should establish context for the scenario.
            {STRUCTURE_SECTION}
            {TENSE_SECTION}
            {REQUIREMENTS_SECTION}
            **Examples:**
            {VALID_EXAMPLES_SECTION}
            {INVALID_EXAMPLES_SECTION}
            **Suggestions/Feedback:**
            * **Remind users that Given steps establish preconditions that exist before the scenario begins.
            {FEEDBACK_SECTION}

            """;

    public static final String WHEN_TEMPLATE = """
            **WHEN STEP GUIDELINES**
            **Data:** The line starts with "When" or the previous line started with "When" and the current line starts with "And".
            **Requirements:**
            {STRUCTURE_SECTION}
            {TENSE_SECTION}
            {REQUIREMENTS_SECTION}
            **Examples:**
            {VALID_EXAMPLES_SECTION}
            {INVALID_EXAMPLES_SECTION}
            **Suggestions/Feedback:**
            * **Remind users that When steps describe actions being undertaken in the current context.
            {FEEDBACK_SECTION}

            """;

    public static final String THEN_TEMPLATE = """
            **THEN STEP GUIDELINES**
            **Data:** The line starts with "Then" or the previous line started with "Then" and the current line starts with "And".
            **Requirements:**
            1. **Matching Actions:**
                * The action mentioned in the Then step should correspond to the action performed in the preceding When step.
            {STRUCTURE_SECTION}
            {TENSE_SECTION}
            {REQUIREMENTS_SECTION}
            **Examples:**
            {VALID_EXAMPLES_SECTION}
            {INVALID_EXAMPLES_SECTION}
            **Suggestions/Feedback:**
            * **Focus on Result:** Emphasize the resulting state or change caused by the action.
            * **Action Match:** Ensure that the action in the Then step matches the actions used in the preceding WHEN steps.
            {FEEDBACK_SECTION}

            """;

    public static final String LLM_INPUT = """
            **INPUT:**
            %s
            """;

    public static final String OUTPUT_FORMAT_JSON =
            "**VALIDATION REPORT FORMAT (JSON STRUCTURE):**\n" +
                    "The output for the Gherkin validation report must be structured as a JSON array containing objects for each line (Scenario or step) in the Gherkin syntax. These objects should be parsable by standard Java JSON parsing libraries like Jackson or Gson.\n\n" +
                    "Each object will have the following properties:\n\n" +
                    "- **title (string, required):** The title of the line. For Scenario lines, it should be the scenario name; for Given, When, Then, And steps, it should be the actual step text.\n" +
                    "- **status (string, required):** Must be either \"Valid\" or \"Invalid\", indicating the validation result for the line.\n" +
                    "- **reason (string, required):** A brief explanation for why the line is invalid. Use \"NA\" for valid lines.\n" +
                    "- **suggestion (string, required):**\n" +
                    "  - For invalid lines: Provide the corrected title of the line following the given standards.\n" +
                    "  - For valid lines:\n" +
                    "    - \"Valid syntax\": Indicates the line adheres to the Gherkin syntax.\n" +
                    "    - \"Consider refactoring\": Suggests potential improvements to the line, even though it's syntactically valid (e.g., clearer wording, better keyword usage).\n" +
                    "    - \"[Specific suggestion]\": Offers a tailored suggestion for improvement (e.g., \"Replace 'then' with 'and' for a better flow\").\n\n" +

                    "**Example:**\n\n" +
                    "```json\n" +
                    "[\n" +
                    "  {\n" +
                    "    \"title\": \"Scenario: Search Morningstar Indexes with valid parameters\",\n" +
                    "    \"status\": \"Valid\",\n" +
                    "    \"reason\": \"\",\n" +
                    "    \"suggestion\": \"\"\n" +
                    "  },\n" +
                    "  {\n" +
                    "    \"title\": \"Given an user is navigated to the morningstar index page\",\n" +
                    "    \"status\": \"Invalid\",\n" +
                    "    \"reason\": \"Missing 'a' before 'user',\n" +
                    "    \"suggestion\": \"Given a user is navigated to the morningstar index page\"\n" +
                    "  }\n" +
                    "  // ... more validation results for other lines ...\n" +
                    "]\n" +
                    "```\n\n" +

                    "**Important Notes:**\n" +
                    "- Ensure the output strictly adheres to the provided JSON structure.\n" +
                    "- The \"status\" field should strictly contain either \"Valid\" or \"Invalid\".\n\n";

    public static final String ENTITIES_TASK = """
            Ensure that the entities used in the Gherkin steps are from the following list: {ENTITIES_LIST}
            """;

    public static final String GENERIC_PROMPT_CUCUMBER = """
            Ensure that the Gherkin syntax is validated based on the best Cucumber BDD recommended format.
            """;

    public static final String GENERIC_PROMPT_JBEHAVE = """
            Ensure that the Gherkin syntax is validated based on the best JBehave BDD recommended format.
            """;

    public static final String GENERIC_PROMPT_BOTH = """
            Ensure that the Gherkin syntax is validated based on the best Cucumber BDD or JBehave BDD recommended format.
            """;

}