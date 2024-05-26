package com.vchatrola.plugin.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * A utility class that holds constants used throughout the GherkinLint plugin.
 */
public class PluginConstants {

    public static final String TOOL_WINDOW_ID = "GherkinLint";
    public static final String CONTENT_DISPLAY_NAME = "Result";
    public static final List<String> SUPPORTED_FILE_EXTENSIONS = ImmutableList.of("story", "feature", "txt");
    public static final List<String> GHERKIN_KEYWORDS = ImmutableList.of
            ("Scenario", "Meta", "Given", "When", "Then", "And", "But");
    public static final String KEYWORD_SCENARIO = "Scenario";
    public static final String KEYWORD_GIVEN = "Given";
    public static final String KEYWORD_WHEN = "When";
    public static final String KEYWORD_THEN = "Then";
    public static final String KEYWORD_AND = "And";
    public static final String TOOL_WINDOW_DEFAULT_MESSAGE = "Select Gherkin text and right-click to validate.";
    public static final String ERROR_MESSAGE_NO_GHERKIN_TEXT_SELECTED = "No Gherkin text selected for validation. Please select " +
            "some text containing a Gherkin statement and try again.";
    public static final String ERROR_MESSAGE_GHERKIN_TEXT_TOO_SHORT = "Selected text is too short. Valid Gherkin text typically " +
            "contains at least 3 words. Please select a longer Gherkin text for validation.";
    public static final String ERROR_MESSAGE_GHERKIN_AND_NO_CONTEXT = "Selected Gherkin text seems to be an 'And' step, " +
            "but it's missing context. 'And' steps rely on previous 'Given', 'When', or 'Then' steps to define the " +
            "scenario flow. Please select a Gherkin snippet that includes both the 'And' step and its preceding steps" +
            " for proper validation.";
    public static final String ERROR_MESSAGE_NO_RESPONSE = "No response received from the Gemini service.";
    public static final String ERROR_MESSAGE_VALIDATION = "An error occurred during Gherkin text validation task execution.";

}
