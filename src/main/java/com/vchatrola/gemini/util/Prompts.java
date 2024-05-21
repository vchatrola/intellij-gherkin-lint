package com.vchatrola.gemini.util;

public class Prompts {

    public static final String GHERKIN_VALIDATION =
            "**Context:** A user is developing a QA automation project using Gherkin syntax. They have selected a portion of their Gherkin script.\n" +
                    "**Data:** The selected text contains multiple lines of Gherkin syntax, potentially with errors.\n" +
                    "**Task:**\n" +
                    "* Analyze each line of the provided text to check for valid Gherkin syntax.\n" +
                    "* Identify any errors, such as invalid keywords or missing elements.\n" +
                    "* For lines with errors, suggest corrections or improvements to achieve valid Gherkin syntax.\n" +
                    "* If a line is valid, consider providing insights or potential completions to enhance the script.\n" +
                    "\n**Input:**\n";

}