package com.vchatrola.prompt;

public class PromptTemplate {

    public static final String CONTEXT_TEMPLATE = """
            **CONTEXT**
            * {CONTEXT}

            **YOUR TASKS**
            {TASKS}
            """;

    public static final String GENERAL_REQUIREMENTS_TEMPLATE = """
            **GENERAL REQUIREMENTS**
            {REQUIREMENTS_SECTION}
            """;

    public static final String SCENARIO_TEMPLATE = """
            **SCENARIO GUIDELINES**
            * **Applicable Line:** Any line starting with "Scenario:" or "Scenario Outline:".
            * **Requirements:**
                1. The scenario title must clearly convey the main idea of the scenario and indicate what the scenario will test.
                {STRUCTURE_SECTION}
                {REQUIREMENTS_SECTION}
            * **Examples:**
                {VALID_EXAMPLES_SECTION}
                {INVALID_EXAMPLES_SECTION}
            """;

    public static final String GIVEN_TEMPLATE = """
            **GIVEN STEP GUIDELINES**
            * **Applicable Line:** Any line starting with "Given" or following a "Given" line with "And," "But," or "*".
            * **Requirements:**
                1. Given step should describe the initial context or state that sets up the necessary conditions for the scenario before the When step action occurs.
                {STRUCTURE_SECTION}
                {TENSE_SECTION}
                {REQUIREMENTS_SECTION}
            * **Examples:**
                {VALID_EXAMPLES_SECTION}
                {INVALID_EXAMPLES_SECTION}
            {FEEDBACK_SECTION}
            """;

    public static final String WHEN_TEMPLATE = """
            **WHEN STEP GUIDELINES**
            * **Applicable Line:** Any line starting with "When" or following a "When" line with "And," "But," or "*".
            * **Requirements:**
                1. When steps should describe the action or event directly related to the scenario's goal, ensuring focus on actions being taken in the current context.
                {STRUCTURE_SECTION}
                {TENSE_SECTION}
                {REQUIREMENTS_SECTION}
            * **Examples:**
                {VALID_EXAMPLES_SECTION}
                {INVALID_EXAMPLES_SECTION}
            {FEEDBACK_SECTION}
            """;

    public static final String THEN_TEMPLATE = """
            **THEN STEP GUIDELINES**
            * **Applicable Line:** Any line starting with "Then" or following a "Then" line with "And," "But," or "*".
            * **Requirements:**
                1. Then step should describe the expected outcome or result, corresponding to the action performed in the preceding When step.
                {STRUCTURE_SECTION}
                {TENSE_SECTION}
                {REQUIREMENTS_SECTION}
            * **Examples:**
                {VALID_EXAMPLES_SECTION}
                {INVALID_EXAMPLES_SECTION}
            {FEEDBACK_SECTION}
            """;

    public static final String TAG_TEMPLATE = """
            **TAG GUIDELINES**
            * **Applicable Line:** Any line starting with "Meta" or "@".
            {REQUIREMENTS_SECTION}
            """;

    public static final String LLM_INPUT = """
            **INPUT:**
            %s
            """;

    public static final String OUTPUT_FORMAT_JSON = """
            **VALIDATION REPORT FORMAT (JSON STRUCTURE):**
            * The output for the Gherkin validation report must be structured as a JSON array containing objects for each line (Scenario or step) in the Gherkin syntax, parseable by standard Java JSON parsing libraries like Jackson.
            * Each object will have the following properties:
                1. **title (string, required):** The title of the line. For Scenario lines, it should be the scenario name; for Given, When, Then, And, But, Meta, *,  steps, it should be the actual step text.
                2. **status (string, required):** Must be either "Valid" or "Invalid", indicating the validation result for the line.
                3. **reason (string, required):** A brief explanation for why the line is invalid. Use "NA" for valid lines.
                4. **suggestion (string, required):**
                    - For invalid lines:
                        - Provide the corrected title of the line following the given standards.
                    - For valid lines:
                        - "Valid syntax": Indicates the line adheres to the Gherkin syntax.
                        - "Consider refactoring": Suggests potential improvements to the line, even though it's syntactically valid (e.g., clearer wording, better keyword usage).
                        - "[Specific suggestion]": Offers a tailored suggestion for improvement (e.g., "Replace 'Then' with 'And' or 'But' for a better flow").
            * **Example:**
                ```json
                [
                  {
                    "title": "Scenario: Search Morningstar Indexes with valid parameters",
                    "status": "Valid",
                    "reason": "",
                    "suggestion": ""
                  },
                  {
                    "title": "Given an user is navigated to the morningstar index page",
                    "status": "Invalid",
                    "reason": "Missing 'a' before 'user'",
                    "suggestion": "Given a user is navigated to the morningstar index page"
                  }
                  // ... more validation results for other lines ...
                ]
                ```
            """;

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

    public static final String PERSPECTIVE_REQUIREMENT = "All the steps must be written in the %s point of view.";

    public static final String BUT_REQUIREMENT = "For \"But\" statements, ensure proper placement after Given, When, "
            + "Then or And. Also Verify it introduces an unexpected or negative outcome related to the preceding statement.";

    public static final String EXAMPLES_REQUIREMENT = "For scenarios with < > placeholders, ensure following " +
            "'Examples:' sections with matching parameter values. Suggest adding 'Examples:' if missing.";

    public static String getStructureInstructions() {
        String indentation = PromptUtils.getIndentation(SCENARIO_TEMPLATE, "{STRUCTURE_SECTION}");
        return "**Structure (Mandatory):**" +
                "\n" + indentation + PromptUtils.generateSpaces(5) +
                "* The %s step must follow one of the following recommended formats:";
    }

}