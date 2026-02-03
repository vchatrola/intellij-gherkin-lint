package com.vchatrola.util;

import java.util.List;

/**
 * A utility class that holds constants used throughout the GherkinLint plugin.
 */
public class Constants {

    public static final String TOOL_WINDOW_ID = "GherkinLint";
    public static final String CONTENT_DISPLAY_NAME = "Result";
    public static final List<String> SUPPORTED_EXTENSIONS = List.of("story", "feature", "txt");
    public static final List<String> GHERKIN_KEYWORDS = List.of
            ("Scenario", "Meta", "Given", "When", "Then", "And", "But", "*", "@");
    public static final String SCENARIO_KEYWORD = "Scenario";
    public static final String GIVEN_KEYWORD = "Given";
    public static final String WHEN_KEYWORD = "When";
    public static final String THEN_KEYWORD = "Then";
    public static final String AND_KEYWORD = "And";
    public static final String BUT_KEYWORD = "But";
    public static final String ASTERISK_KEYWORD = "*";
    public static final String PROPERTY_TITLE = "Title";
    public static final String PROPERTY_STATUS = "Status";
    public static final String PROPERTY_REASON = "Reason";
    public static final String PROPERTY_SUGGESTION = "Suggestion";
    public static final String STATUS_VALID = "Valid";
    public static final String STATUS_INVALID = "Invalid";
    public static final String DEFAULT_TOOL_WINDOW_TEXT = "Select Gherkin text and right-click to validate.";
    public static final String NO_GHERKIN_TEXT_SELECTED_ERROR = "No Gherkin text selected for validation. Please select " +
            "some text containing a Gherkin statement and try again.";
    public static final String GHERKIN_TEXT_TOO_SHORT_ERROR = "Selected text is too short. Valid Gherkin text typically " +
            "contains at least 3 words. Please select a longer Gherkin text for validation.";
    public static final String GHERKIN_NO_CONTEXT_ERROR = "The selected Gherkin text includes an 'And', 'But', or '*' step without context. " +
            "These steps depend on preceding 'Given', 'When', or 'Then' steps to define the scenario flow. " +
            "Please include the preceding steps for proper validation.";
    public static final long VALIDATION_THROTTLE_MS = 5000L;
    public static final String VALIDATION_THROTTLED_ERROR = "Please wait a moment before validating again.";
    public static final String NO_GEMINI_SERVICE_RESPONSE_ERROR = "No response received from the Gemini service.";
    public static final String UNKNOWN_ERROR = "An error occurred during Gherkin text validation task execution.";
    public static final String CONSOLE_OUTPUT_PRINT_FAILURE = "Failed to print console output.";
    public static final String GEMINI_SERVICE_ACCESS_ERROR = "Unable to access GeminiService. Please ensure the service is properly " +
            "configured and try again.";
}
