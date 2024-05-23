package com.vchatrola.gemini.util;

public class Prompts {

    public static final String CONTEXT = """
            **Context:** A user is developing a QA automation project using Gherkin syntax. They have selected a portion of their Gherkin script.
            **Data:** The selected text contains multiple lines of Gherkin syntax, potentially with errors.
            **Task:**
            * Analyze each line of the provided text to check for valid Gherkin syntax.
            * Identify any errors, such as invalid keywords or missing elements.
            * For lines with errors, suggest corrections or improvements to achieve valid Gherkin syntax, adhering to the specific guidelines provided for each statement type (Scenario, Given, When, Then).
            * If a line is valid, consider providing insights or potential completions to enhance the script.

            **Note:**
            * Separate prompts with detailed guidelines for validating Scenario, Given, When, and Then statements are given below:
            * Format the output according to the provided format guidelines.

            """;

    public static final String SCENARIO = """
            **Scenario Validation Guide**

            **Data:**
            The selected line starts with "Scenario:".

            **Requirements/Rules:**
            1. **Check Starting Keyword:** Verify if the line begins with "Scenario:".
            2. **Analyze Structure (Mandatory):** Analyze the line to see if it follows the recommended structure: "{Action} {Entity} {Association}{Condition}". This structure is mandatory. However, if enforcing the structure results in a sentence that does not make sense and the user's approach is clearer, allow the user's approach but provide reasons for the exception.
            3. **Focus on Clarity and Conciseness:** Evaluate if the title clearly communicates the scenario's purpose without unnecessary details.

            **Requirements/Rules:**
            1. **Syntax:**
                * The scenario title must begin with the keyword "Scenario:".
            2. **Structure (Mandatory):**
                * Analyze the line to see if it follows the recommended structure: "{Action} {Entity} {Association}{Condition}".
                * This structure is mandatory. If the user's approach is clearer but deviates from the structure, allow it with an explanation of the recommended structure and the rationale for the exception.
            3. **Clarity and Conciseness:**
                * The scenario title should clearly communicate the scenario's purpose without unnecessary details.

            **Examples:**
            * **Valid:**
                * Scenario: Enroll a youth student into a section (Clear and concise)
                * Scenario: Enroll student with warning profile hold into a section (Clear action and entity)
                * Scenario: Enroll a student into a section with flat fee (Clear and concise)
                * Scenario: Enroll a student into a section with flat fee that is available for family checkout (Includes condition)
            * **Invalid:**
                * Scenario: Youth section enrollment (Vague and lacks details) - This scenario is invalid because it doesn't clearly state the action or entity involved. A better option would be:
                    * Scenario: Enroll a youth student into a section (Clear action and entity)
                * Scenario: As a student with a profile hold, I still want to purchase a section in public site to enroll into the section (Too long and includes user role) - This scenario is invalid because it's overly verbose and includes unnecessary details. A better option would be:
                    * Scenario: Enroll a student with warning profile hold into a section (Focuses on core action and entity)
                * Scenario: Section with flat fee enrollment (Focuses on section, not action) - This scenario is invalid because it focuses on the section instead of the action being performed. A better option would be:
                    * Scenario: Enroll a student into a section with flat fee (Clear action and entity)

            **Suggestions/Feedback:**
            If the line deviates from the standards based on Requirements/Rules, suggest improvements based on the provided examples. Here are some additional considerations:
            * **Vague Titles:** Expand on the action and entity involved (e.g., "Update" instead of "Change").
            * **Overly Long Titles:** Focus on the core action and entity, omitting unnecessary details.
            * **Structure Deviations:** If possible, suggest a title that aligns with the structure while maintaining clarity. However, prioritize clear communication when a perfect fit isn't achievable.
            
            """;

    public static final String GIVEN = """
            **Given Step Validation Guide**

            **Data:**
            The line starts with "Given" or the previous line started with "Given" and the current line starts with "And".

            **Requirements/Rules:**
            1. **Check Starting Keyword:** Verify if the line begins with "Given" or if the previous line started with "Given" and the current line starts with "And".
            2. **Structure (Mandatory):** The Given step should follow one of the following recommended formats:
                * "{there exists/a/an/} {entity} {with/having} {Association}{Condition}"
                * "{Entity} {has performed an action} {entity 1}"
                * This structure is mandatory. If the user's approach is clearer but deviates from the structure, allow it with an explanation of the recommended structure and the rationale for the exception.
            3. **Tense:** Ensure that Given statements are in past tense or past perfect tense, signifying an event, precondition, or system state that already exists.

            **Examples:**
            * **Valid:**
                * Given there exists a section with an active waitlist (Uses recommended structure and past tense)
                * Given the student has added the section to the cart (Uses recommended structure and past perfect tense)
            * **Invalid:**
                * Given a section that is accepting wait list students - This statement is invalid because it does not use the correct structure. A better option would be:
                    * Given there exists a section with an active waitlist
                * Given the student adds a section to the cart - This statement is invalid because it does not use the correct tense. A better option would be:
                    * Given the student has added the section to the cart

            **Suggestions/Feedback:**
            If the line deviates from the standards based on Requirements/Rules, suggest improvements based on the provided examples. Here are some additional considerations:
            * **If the Given step deviates from the recommended structure or tense, suggest improvements based on the provided examples.
            * **Clarity and Conciseness:** Ensure the statement clearly communicates the precondition or system state without unnecessary details.
            * **Remind users that Given steps establish preconditions that exist before the scenario begins.
            
            """;

    public static final String WHEN = """
            **When Step Validation Guide**

            **Data:**
            The line starts with "When" or the previous line started with "When" and the current line starts with "And".

            **Requirements/Rules:**
            1. **Check Starting Keyword:** Verify if the line begins with "When" or if the previous line started with "When" and the current line starts with "And".
            2. **Structure (Mandatory):** The When step should follow the following recommended format:
                * `the {entity} {performs action with/on} {entity 1} {entity 2}`
                * This structure is mandatory. If the user's approach is clearer but deviates from the structure, allow it with an explanation of the recommended structure and the rationale for the exception.
            3. **Tense:** The When step should be written in the present tense, signifying an action that is being undertaken by a user/system in the current context.
            4. **Single Action:** A single When statement should describe a single action or behavior. If multiple actions are involved, consider splitting them into separate When steps.
            5. **Definite Article:** Use "the" instead of "a" to identify with the entity established in the Given steps. This facilitates referencing complex entities with conditions and associations.

            **Examples:**
            * **Valid:**
                * When the student purchases the section by deposit (Uses recommended structure and present tense)
                * When the user submits the form (Uses recommended structure and present tense)
            * **Invalid:**
                * When a student will purchase a section by deposit - This statement is invalid because it uses future tense and lacks the correct structure. A better option would be:
                    * When the student purchases the section by deposit
                * When the student navigates to the section profile and adds the section to the cart - This statement is invalid because it contains multiple actions. A better option would be:
                    * When the student adds the section to the cart (This already implies the act of searching for the section, navigating to the section profile, and adding the section to the cart)

            **Suggestions/Feedback:**
            If the line deviates from the standards based on Requirements/Rules, suggest improvements based on the provided examples. Here are some additional considerations:
            * **If the When step deviates from the recommended structure or tense, suggest improvements based on the provided examples.
            * **Clarity and Conciseness:** Ensure the statement clearly describes the action being performed without unnecessary details.
            * ** Remind users that When steps describe actions being undertaken in the current context.
            
            """;

    public static final String THEN = """
            **Then Step Validation Guide**

            **Data:**
            The line starts with "Then" or the previous line started with "Then" and the current line starts with "And".

            **Requirements/Rules:**
            1. **Check Starting Keyword:** Verify if the line begins with "Then" or if the previous line started with "Then" and the current line starts with "And".
            2. **Structure (Mandatory):** The Then step should follow one of the following recommended formats:
                * `the {action} is {successful/fail/error is displayed}`
                * `the {entity} {action outcome} {entity 1}`
                * `the validation error message for <action outcome> <validation condition>`
                * This structure is mandatory. If the user's approach is clearer but deviates from the structure, allow it with an explanation of the recommended structure and the rationale for the exception.
            3. **Tense:** The Then step can be written in either present or future tense, signifying the expected outcome or result of the action taken in the When step.
            4. **Matching Actions:** The action mentioned in the Then step should correspond to the action performed in the preceding When step.

            **Examples:**
            * **Valid:**
                * Then the purchase is successful (Uses recommended structure and refers to action from When)
                * Then the student is enrolled in the section (Uses recommended structure and refers to entity from Given)
                * Then the validation error message for section not available is displayed (Uses recommended structure and present tense)
            * **Invalid:**
                * Then the enrollment to the section is successful - This statement is invalid because "enrollment to the section" is not a valid action. A better option would be:
                    * Then the purchase is successful
                * And the section has a new student (Incorrect structure) - This statement is invalid because it does not use the correct structure. A better option would be:
                    * And the student is enrolled in the section

            **Suggestions/Feedback:**
            If the line deviates from the standards based on Requirements/Rules, suggest improvements based on the provided examples. Here are some additional considerations:
            * **If the Then step deviates from the recommended structure or tense, suggest improvements based on the provided examples.
            * **Clarity and Conciseness:** Ensure the statement clearly describes the expected outcome without unnecessary details.
            * **Focus on Result:** Emphasize the resulting state or change caused by the action.
            * **Action Match:** Ensure that the action in the Then step matches the actions used in the preceding WHEN steps.
            
            """;

    public static final String LLM_INPUT = """
            ***Input:***
            %s
            """;

    public static final String OUTPUT_FORMAT = """
            **Output Format:**
            * Provide feedback in the following format:
              * **Scenario Title**
                - Status: Valid/Invalid
                - Reason: Brief explanation of why the title is valid or invalid.
                - Suggestion: Provide a corrected or improved title if invalid, or a confirmation if valid.
              * For each step (Given, When, Then, And, But), include the following only if the step is present:
                **Step:** The Gherkin step being validated (Given, When, Then, And, But).
                - Status: Valid/Invalid
                - *Reason: Brief explanation of why the step is valid or invalid.
                - Suggestion: Suggested correction if the step is invalid.
              * ***Overall Summary***
                - Overall Status: Indicate if the entire scenario is valid or if there are issues.
                - General Suggestions: Any overarching suggestions for improving the Gherkin scenario.
              * If any of the elements are not present in the input text, skip that part in the output.
            
            """;

}