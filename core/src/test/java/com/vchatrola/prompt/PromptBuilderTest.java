package com.vchatrola.prompt;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PromptBuilderTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void buildContext_includesGenericPromptForDefaultValidation() throws Exception {
        PromptBuilder builder = new PromptBuilder(buildConfig(), "feature");
        String context = builder.buildContext(true);

        assertTrue(context.contains("Test context"));
        assertTrue(context.contains(PromptTemplate.GENERIC_PROMPT_CUCUMBER.trim()));
    }

    @Test
    void buildContext_excludesGenericPromptWhenNotDefault() throws Exception {
        PromptBuilder builder = new PromptBuilder(buildConfig(), "feature");
        String context = builder.buildContext(false);

        assertTrue(context.contains("Test context"));
        assertFalse(context.contains(PromptTemplate.GENERIC_PROMPT_CUCUMBER.trim()));
    }

    @Test
    void buildPrompt_includesTagContextWhenTagPresent() throws Exception {
        PromptBuilder builder = new PromptBuilder(buildConfig(), "feature");
        String prompt = builder.buildPrompt("@tag\nScenario: Test", true);

        assertTrue(prompt.contains("TAG GUIDELINES"));
        assertTrue(prompt.contains("Tag requirement"));
        assertTrue(prompt.contains("@tag"));
    }

    @Test
    void buildPrompt_handlesMissingSectionsGracefully() throws Exception {
        JsonNode minimal = mapper.readTree("""
                {
                  "CONTEXT": "Test context",
                  "TASKS": ["Task one"],
                  "ENTITIES": [],
                  "REQUIREMENTS": [],
                  "PERSPECTIVE": "",
                  "SCENARIO": {},
                  "GIVEN": {},
                  "WHEN": {},
                  "THEN": {},
                  "TAG": {}
                }
                """);
        PromptBuilder builder = new PromptBuilder(minimal, "feature");
        String prompt = builder.buildPrompt("Scenario: Test", true);

        assertTrue(prompt.contains("Test context"));
        assertTrue(prompt.contains("VALIDATION REPORT FORMAT"));
        assertFalse(prompt.contains("{STRUCTURE_SECTION}"));
        assertFalse(prompt.contains("{REQUIREMENTS_SECTION}"));
    }

    @Test
    void buildPrompt_handlesInvalidExampleSchema() throws Exception {
        JsonNode config = mapper.readTree("""
                        {
                          "CONTEXT": "Test context",
                          "TASKS": ["Task one"],
                          "ENTITIES": ["User"],
                          "REQUIREMENTS": ["Requirement one"],
                          "PERSPECTIVE": "tester",
                          "SCENARIO": {
                            "examples": {
                              "invalid": [ { "example": "Bad example" } ]
                            }
                          },
                          "GIVEN": {},
                          "WHEN": {},
                          "THEN": {},
                          "TAG": {}
                        }
                """);
        PromptBuilder builder = new PromptBuilder(config, "feature");
        String prompt = builder.buildPrompt("Scenario: Test", true);

        assertTrue(prompt.contains("Bad example"));
        assertTrue(prompt.contains("Reason: "));
        assertTrue(prompt.contains("Suggestion: "));
    }

    private JsonNode buildConfig() throws Exception {
        return mapper.readTree("""
                {
                  "CONTEXT": "Test context",
                  "TASKS": ["Task one"],
                  "ENTITIES": ["User", "Order"],
                  "REQUIREMENTS": ["Requirement one"],
                  "PERSPECTIVE": "tester",
                  "SCENARIO": {
                    "structure": ["Scenario structure"],
                    "requirements": ["Scenario requirement"],
                    "examples": {
                      "valid": ["Scenario valid"],
                      "invalid": [
                        { "example": "Scenario bad", "reason": "Reason", "suggestion": "Fix" }
                      ]
                    }
                  },
                  "GIVEN": {
                    "structure": ["Given structure"],
                    "requirements": ["Given requirement"],
                    "tense": "past tense",
                    "examples": { "valid": ["Given valid"] },
                    "feedback": ["Given feedback"]
                  },
                  "WHEN": {
                    "structure": ["When structure"],
                    "requirements": ["When requirement"],
                    "tense": "present tense"
                  },
                  "THEN": {
                    "structure": ["Then structure"],
                    "requirements": ["Then requirement"],
                    "tense": "future tense"
                  },
                  "TAG": {
                    "requirements": ["Tag requirement"]
                  }
                }
                """);
    }
}
