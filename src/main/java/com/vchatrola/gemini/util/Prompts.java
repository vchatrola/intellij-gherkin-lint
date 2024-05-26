package com.vchatrola.gemini.util;

public class Prompts {

    public static final String CONTEXT = """
            **CONTEXT**
            * A QA engineer is working on a software test automation project using a BDD (Behavior-Driven Development) framework and needs to validate a selected portion of Gherkin syntax, which may have errors.

            **TASK:**
            * Validate each line (Scenario, Given, When, Then) based on the provided guidelines.
                * Check for syntax errors, tense, invalid keywords or missing elements, English grammar issues (articles, spellings), and determine validity.
            * For lines with errors, suggest corrections to achieve valid Gherkin syntax, adhering to the specific guidelines provided for each statement type (Scenario, Given, When, Then).
            * If a line is valid, consider providing insights or potential completions to enhance the script.

            """;

    public static final String SCENARIO = """
            **SCENARIO GUIDELINES**
            **Data:** The selected line starts with "Scenario:".
            **Requirements:**
            1. **Syntax:**
                * The scenario title must begin with the keyword "Scenario:".
            2. **Structure (Mandatory):**
                * Follow the recommended structure: "{Action} {Entity} {Association}{Condition}".
                * This structure is mandatory. Deviations allowed with explanation of recommended structure.
            3. **Clarity and Conciseness:**
                * The scenario title should clearly communicate the scenario's purpose without unnecessary details.
            **Examples:**
            * **Valid:**
                * Scenario: Enroll a youth student into a section
                * Scenario: Enroll a student with warning profile hold into a section
                * Scenario: Enroll a student into a section with flat fee
                * Scenario: Enroll a student into a section with flat fee that is available for family checkout (Includes condition)
            * **Invalid:**
                * Scenario: Youth section enrollment (Vague and lacks details) - It doesn't starts with an action. A better option would be:
                    * Scenario: Enroll a youth student into a section (Clear action and entity)
                * Scenario: As a student with a profile hold, I still want to purchase a section in public site to enroll into the section (Too long and includes user role) - It's overly verbose and includes unnecessary details. A better option would be:
                    * Scenario: Enroll a student with warning profile hold into a section (Focuses on core action and entity)
                * Scenario: Section with flat fee enrollment (Focuses on section, not action) - It focuses on the section instead of the action being performed. A better option would be:
                    * Scenario: Enroll a student into a section with flat fee (Clear action and entity)

            """;

    public static final String GIVEN = """
            **GIVEN STEP GUIDELINES**
            **Data:** The line starts with "Given" or the previous line started with "Given" and the current line starts with "And".
            **Requirements:**
            1. **Check Starting Keyword:**
                * Verify if the line begins with "Given" or if the previous line started with "Given" and the current line starts with "And".
            2. **Structure (Mandatory):** The Given step should follow one of the following recommended formats:
                * "{there exists/a/an/} {entity} {with/having} {Association}{Condition}"
                * "{Entity} {has performed an action} {entity 1}"
                * This structure is mandatory. Deviations allowed with explanation of recommended structure.
            3. **Tense:**
                * Ensure that Given statements are in past tense or past perfect tense, signifying an event, precondition, or system state that already exists.
            **Examples:**
            * **Valid:**
                * Given there exists a section with an active waitlist (Uses recommended structure and past tense)
                * Given the student has added the section to the cart (Uses recommended structure and past perfect tense)
            * **Invalid:**
                * Given a section that is accepting wait list students - It does not use the correct structure. A better option would be:
                    * Given there exists a section with an active waitlist
                * Given the student adds a section to the cart - It does not use the correct tense. A better option would be:
                    * Given the student has added the section to the cart
            **Suggestions/Feedback:**
            * **Remind users that Given steps establish preconditions that exist before the scenario begins.

            """;

    public static final String WHEN = """
            **WHEN STEP GUIDELINES**
            **Data:** he line starts with "When" or the previous line started with "When" and the current line starts with "And".
            **Requirements:**
            1. **Check Starting Keyword:**
                * Verify if the line begins with "When" or if the previous line started with "When" and the current line starts with "And".
            2. **Structure (Mandatory):** The When step should follow the following recommended format:
                * `the {entity} {performs action with/on} {entity 1} {entity 2}`
                * This structure is mandatory. Deviations allowed with explanation of recommended structure.
            3. **Tense:**
                * The When step should be written in the present tense, signifying an action that is being undertaken by a user/system in the current context.
            4. **Single Action:** A single When statement should describe a single action or behavior. If multiple actions are involved, consider splitting them into separate When steps.
            5. **Definite Article:** Use "the" instead of "a" to identify with the entity established in the Given steps (Very Important).
            **Examples:**
            * **Valid:**
                * When the student purchases the section by deposit (Uses recommended structure and present tense)
                * When the user submits the form (Uses recommended structure and present tense)
            * **Invalid:**
                * When a student will purchase a section by deposit - It uses future tense and lacks the correct structure. A better option would be:
                    * When the student purchases the section by deposit
                * When the student navigates to the section profile and adds the section to the cart - It contains multiple actions. A better option would be:
                    * When the student adds the section to the cart (This already implies the act of searching for the section, navigating to the section profile, and adding the section to the cart)
            **Suggestions/Feedback:**
            * ** Remind users that When steps describe actions being undertaken in the current context.

            """;

    public static final String THEN = """
            **THEN STEP GUIDELINES**
            **Data:** The line starts with "Then" or the previous line started with "Then" and the current line starts with "And".
            **Requirements:**
            1. **Check Starting Keyword:**
                * Verify if the line begins with "Then" or if the previous line started with "Then" and the current line starts with "And".
            2. **Structure (Mandatory):** The Then step should follow one of the following recommended formats:
                * `the {action} is {successful/fail/error is displayed}`
                * `the {entity} {action outcome} {entity 1}`
                * `the validation error message for <action outcome> <validation condition>`
                * This structure is mandatory. Deviations allowed with explanation of recommended structure.
            3. **Tense:**
                * The Then step can be written in either present or future tense, signifying the expected outcome or result of the action taken in the When step.
            4. **Matching Actions:**
                * The action mentioned in the Then step should correspond to the action performed in the preceding When step.
            **Examples:**
            * **Valid:**
                * Then the purchase is successful (Uses recommended structure and refers to action from When)
                * Then the student is enrolled in the section (Uses recommended structure and refers to entity from Given)
                * Then the validation error message for section not available is displayed (Uses recommended structure and present tense)
            * **Invalid:**
                * Then the enrollment to the section is successful - "enrollment to the section" is not a valid action. A better option would be:
                    * Then the purchase is successful
                * And the section has a new student (Incorrect structure) - It does not use the correct structure. A better option would be:
                    * And the student is enrolled in the section
            **Suggestions/Feedback:**
            * **Focus on Result:** Emphasize the resulting state or change caused by the action.
            * **Action Match:** Ensure that the action in the Then step matches the actions used in the preceding WHEN steps.

            """;

    public static final String LLM_INPUT = """
            **INPUT:**
            %s
            """;

    public static final String OUTPUT_FORMAT = """
            **Validation Report Format:**

            * If a scenario is present:
            * (**Optional:** Only include if the scenario exists)

                Scenario: [Scenario title]
                - Status: [Valid / Invalid]
                - Reason: [Brief explanation]
                - Suggestion: [Corrected title if invalid or confirmation/suggestion if valid]

            * For each step (Given, When, Then, And, But) within a scenario (if present):
            * (**Optional:** Only include if the step exists)

                Step: [Step Text]
                - Status: [Valid / Invalid]
                - Reason: [Brief explanation]
                - Suggestion: [Corrected title (if invalid) or confirmation/suggestion (if valid)]

            """;

    public static final String OUTPUT_FORMAT_JSON = "The output for the Gherkin validation report must be in valid JSON format, parsable by standard Java JSON parsing libraries like Jackson or Gson.\n\n" +
            "**Validation Report Format (JSON Structure):**\n" +
            "\n" +
            "{\n" +
            "  \"scenarios\": [\n" +
            "    {  // Include only if the scenario starts with \"Scenario:\"\n" +
            "      \"title\": \"[Scenario title]\", (Required)\n" +
            "      \"status\": \"Valid/Invalid\", (Required)\n" +
            "      \"reason\": \"[Brief explanation]\", (Required)\n" +
            "      \"suggestion\": \"[Corrected title if invalid or confirmation/suggestion if valid]\", (Required)\n" +
            "      \"steps\": [  // Include only if there are steps in the scenario\n" +
            "        { \n" +
            "          \"text\": \"[Step Text]\", (Required)\n" +
            "          \"status\": \"Valid/Invalid\", (Required)\n" +
            "          \"reason\": \"[Brief explanation]\", (Required)\n" +
            "          \"suggestion\": \"Corrected title if invalid or confirmation/suggestion if valid]\", (Required)\n" +
            "        },\n" +
            "        // ... more steps within the scenario (if present)\n" +
            "      ]\n" +
            "    },\n" +
            "    // ... more scenarios (if present)\n" +
            "  ]\n" +
            "}\n" +
            "\n" +
            "**Important Notes:**\n" +
            "* Do not output anything other than the JSON data representing the validation report.\n" +
            "* Include scenarios and steps in the output only if they are present in the given Gherkin input data.\n" +
            "* All elements within a scenario or step section are mandatory except for the \"suggestion\" field.\n" +
            "* The \"status\" field should strictly contain either \"Valid\" or \"Invalid\".";

}