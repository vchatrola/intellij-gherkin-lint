package com.vchatrola.prompt;

import com.vchatrola.util.GherkinLintLogger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PromptUtils {

    public static String removeEmptyLines(String input) {
        Pattern pattern = Pattern.compile("(?m)^\\s*$[\r\n]*");
        Matcher matcher = pattern.matcher(input);
        return matcher.replaceAll("");
    }

    public static String generateSpaces(int numSpaces) {
        if (numSpaces < 0) {
            GherkinLintLogger.error("Number of spaces cannot be negative");
            return "";
        }

        return " ".repeat(numSpaces);
    }

    public static String getIndentation(String template, String placeholder) {
        String escapedPlaceholder = Pattern.quote(placeholder);
        String indentationPattern = String.format("^(\\s+)(%s)$", escapedPlaceholder);
        Matcher matcher = Pattern.compile(indentationPattern, Pattern.MULTILINE).matcher(template);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    public static String getLastLineIndentation(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // Regex to match the last non-empty line and capture its indentation
        Pattern pattern = Pattern.compile("(?m)^(\\s*).*\\S.*$");
        Matcher matcher = pattern.matcher(input);

        String lastIndentation = "";
        while (matcher.find()) {
            lastIndentation = matcher.group(1);
        }

        return lastIndentation;
    }

    public static boolean hasAngleBracketPlaceholders(String text) {
        int openingIndex = text.indexOf('<');
        int closingIndex = text.indexOf('>');
        return openingIndex != -1 && closingIndex != -1 && openingIndex < closingIndex;
    }

}
