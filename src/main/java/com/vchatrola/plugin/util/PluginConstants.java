package com.vchatrola.plugin.util;

import com.google.common.collect.ImmutableList;

import java.util.List;

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

}
