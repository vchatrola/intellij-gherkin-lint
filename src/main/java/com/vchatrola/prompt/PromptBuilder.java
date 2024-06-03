package com.vchatrola.prompt;

import com.fasterxml.jackson.databind.JsonNode;
import com.vchatrola.util.Constants;

public class PromptBuilder {

    private final JsonNode config;

    public PromptBuilder(JsonNode config) {
        this.config = config;
    }

    public String buildContext(String fileExtension, boolean isDefaultValidation) {
        String context = PromptTemplate.CONTEXT_TEMPLATE;

        // Replace {CONTEXT} placeholder
        context = context.replace("{CONTEXT}", config.get("CONTEXT").asText());

        // Replace {TASKS} placeholder with numbered tasks
        StringBuilder tasksBuilder = new StringBuilder();
        int taskNumber = 1;
        for (JsonNode task : config.get("TASKS")) {
            tasksBuilder.append(taskNumber).append(". ").append(task.asText()).append("\n");
            taskNumber++;
        }

        // Add entities task if entities are present in the JSON
        JsonNode entitiesNode = config.get("ENTITIES");
        if (entitiesNode != null && entitiesNode.isArray() && !entitiesNode.isEmpty()) {
            StringBuilder entitiesListBuilder = new StringBuilder();
            for (JsonNode entity : entitiesNode) {
                entitiesListBuilder.append(entity.asText()).append(", ");
            }
            // Remove the trailing comma and space
            String entitiesList = entitiesListBuilder.toString().replaceAll(", $", "");
            String entitiesTask = PromptTemplate.ENTITIES_TASK.replace("{ENTITIES_LIST}", entitiesList);

            tasksBuilder.append(taskNumber++).append(". ").append(entitiesTask).append("\n");
        }

        // Add a generic validation task if the context is insufficient
        if (isDefaultValidation) {
            String genericPrompt = switch (fileExtension.toLowerCase()) {
                case "feature" -> PromptTemplate.GENERIC_PROMPT_CUCUMBER;
                case "story" -> PromptTemplate.GENERIC_PROMPT_JBEHAVE;
                default -> PromptTemplate.GENERIC_PROMPT_BOTH;
            };
            tasksBuilder.append(taskNumber++).append(". ").append(genericPrompt).append("\n");
        }

        context = context.replace("{TASKS}", tasksBuilder.toString().trim());

        return context;
    }

    public String buildScenarioContext() {
        String scenarioTemplate = PromptTemplate.SCENARIO_TEMPLATE;
        JsonNode scenarioNode = config.get("SCENARIO");
        int sectionNumber = 3; // Starting from 3 because the first two requirements are fixed

        scenarioTemplate = replacePlaceholder(scenarioTemplate, scenarioNode, "structure",
                "**Structure (Mandatory):** The Scenario step should follow one of the following" +
                        "recommended formats:", sectionNumber++);
        scenarioTemplate = replacePlaceholder(scenarioTemplate, scenarioNode, "requirements",
                "**Additional Requirements:**", sectionNumber++);
        scenarioTemplate = replaceExamples(scenarioTemplate, scenarioNode);

        return scenarioTemplate;
    }

    public String buildGivenContext() {
        String givenTemplate = PromptTemplate.GIVEN_TEMPLATE;
        JsonNode givenNode = config.get("GIVEN");
        int sectionNumber = 2; // Starting from 2 because the first requirement is fixed

        givenTemplate = replacePlaceholder(givenTemplate, givenNode, "structure",
                "**Structure (Mandatory):** The Given step should follow one of the following recommended formats:", sectionNumber++);

        if (givenNode.has("tense") && !givenNode.get("tense").asText().isBlank()) {
            String tenseSection = sectionNumber + ". **Tense:**\n    * Ensure that Given statements are in the "
                    + givenNode.get("tense").asText() + "\n";
            givenTemplate = givenTemplate.replace("{TENSE_SECTION}", tenseSection.trim());
            sectionNumber++;
        } else {
            givenTemplate = givenTemplate.replace("{TENSE_SECTION}", "");
        }

        givenTemplate = replacePlaceholder(givenTemplate, givenNode, "requirements",
                "**Additional Requirements:**", sectionNumber++);

        givenTemplate = replaceExamples(givenTemplate, givenNode);

        if (givenNode.has("feedback") && givenNode.get("feedback").isArray() && !givenNode.get("feedback").isEmpty()) {
            StringBuilder feedbackBuilder = new StringBuilder();
            for (JsonNode feedback : givenNode.get("feedback")) {
                feedbackBuilder.append("* ").append(feedback.asText()).append("\n");
            }
            givenTemplate = givenTemplate.replace("{FEEDBACK_SECTION}", feedbackBuilder.toString().trim());
        } else {
            givenTemplate = givenTemplate.replace("{FEEDBACK_SECTION}", "");
        }

        return givenTemplate;
    }

    public String buildWhenContext() {
        String whenTemplate = PromptTemplate.WHEN_TEMPLATE;
        JsonNode whenNode = config.get("WHEN");
        int sectionNumber = 1;

        whenTemplate = replacePlaceholder(whenTemplate, whenNode, "structure",
                "**Structure (Mandatory):** The When step should follow one of the following recommended formats:", sectionNumber++);

        if (whenNode.has("tense") && !whenNode.get("tense").asText().isBlank()) {
            String tenseSection = sectionNumber + ". **Tense:**\n    * Ensure that When statements are in the "
                    + whenNode.get("tense").asText() + "\n";
            whenTemplate = whenTemplate.replace("{TENSE_SECTION}", tenseSection.trim());
            sectionNumber++;
        } else {
            whenTemplate = whenTemplate.replace("{TENSE_SECTION}", "");
        }

        whenTemplate = replacePlaceholder(whenTemplate, whenNode, "requirements",
                "**Additional Requirements:**", sectionNumber++);

        whenTemplate = replaceExamples(whenTemplate, whenNode);

        if (whenNode.has("feedback") && whenNode.get("feedback").isArray() && !whenNode.get("feedback").isEmpty()) {
            StringBuilder feedbackBuilder = new StringBuilder();
            for (JsonNode feedback : whenNode.get("feedback")) {
                feedbackBuilder.append("* ").append(feedback.asText()).append("\n");
            }
            whenTemplate = whenTemplate.replace("{FEEDBACK_SECTION}", feedbackBuilder.toString().trim());
        } else {
            whenTemplate = whenTemplate.replace("{FEEDBACK_SECTION}", "");
        }

        return whenTemplate;
    }

    public String buildThenContext() {
        String thenTemplate = PromptTemplate.THEN_TEMPLATE;
        JsonNode thenNode = config.get("THEN");
        int sectionNumber = 2; // Starting from 2 because the first requirement is fixed

        thenTemplate = replacePlaceholder(thenTemplate, thenNode, "structure",
                "**Structure (Mandatory):** The Then step should follow one of the following recommended formats:", sectionNumber++);

        if (thenNode.has("tense") && !thenNode.get("tense").asText().isBlank()) {
            String tenseSection = sectionNumber + ". **Tense:**\n    * Ensure that Then statements are in the "
                    + thenNode.get("tense").asText() + "\n";
            thenTemplate = thenTemplate.replace("{TENSE_SECTION}", tenseSection.trim());
            sectionNumber++;
        } else {
            thenTemplate = thenTemplate.replace("{TENSE_SECTION}", "");
        }

        thenTemplate = replacePlaceholder(thenTemplate, thenNode, "requirements",
                "**Additional Requirements:**", sectionNumber++);

        thenTemplate = replaceExamples(thenTemplate, thenNode);

        if (thenNode.has("feedback") && thenNode.get("feedback").isArray() && !thenNode.get("feedback").isEmpty()) {
            StringBuilder feedbackBuilder = new StringBuilder();
            for (JsonNode feedback : thenNode.get("feedback")) {
                feedbackBuilder.append("* ").append(feedback.asText()).append("\n");
            }
            thenTemplate = thenTemplate.replace("{FEEDBACK_SECTION}", feedbackBuilder.toString().trim());
        } else {
            thenTemplate = thenTemplate.replace("{FEEDBACK_SECTION}", "");
        }

        return thenTemplate;
    }

    private String replacePlaceholder(String template, JsonNode node, String sectionKey, String sectionHeader, int sectionNumber) {
        if (node.has(sectionKey) && node.get(sectionKey).isArray() && !node.get(sectionKey).isEmpty()) {
            StringBuilder sectionBuilder = new StringBuilder(sectionNumber + ". " + sectionHeader + "\n");
            for (JsonNode element : node.get(sectionKey)) {
                sectionBuilder.append("    * \"").append(element.asText()).append("\"\n");
            }
            return template.replace("{" + sectionKey.toUpperCase() + "_SECTION}", sectionBuilder.toString().trim());
        } else {
            return template.replace("{" + sectionKey.toUpperCase() + "_SECTION}", "");
        }
    }

    private String replaceExamples(String template, JsonNode parentNode) {
        boolean hasExample = false;

        if (!parentNode.has("examples")) {
            return template.replace("**Examples:**", "");
        }
        JsonNode examplesNode = parentNode.get("examples");

        if (examplesNode.has("valid") && examplesNode.get("valid").isArray() && !examplesNode.get("valid").isEmpty()) {
            StringBuilder validExamplesBuilder = new StringBuilder("**Valid:**\n");
            int number = 1;
            for (JsonNode validExample : examplesNode.get("valid")) {
                validExamplesBuilder.append(number++).append(". ").append(validExample.asText()).append("\n");
            }
            template = template.replace("{VALID_EXAMPLES_SECTION}", validExamplesBuilder.toString().trim());
            hasExample = true;
        } else {
            template = template.replace("{VALID_EXAMPLES_SECTION}", "");
        }

        if (examplesNode.has("invalid") && examplesNode.get("invalid").isArray() && !examplesNode.get("invalid").isEmpty()) {
            StringBuilder invalidExamplesBuilder = new StringBuilder("**Invalid:**\n");
            int number = 1;
            for (JsonNode invalidExample : examplesNode.get("invalid")) {
                invalidExamplesBuilder.append(number++).append(". ").append(invalidExample.get("example").asText())
                        .append(" (Reason: ").append(invalidExample.get("reason").asText())
                        .append(", Suggestion: ").append(invalidExample.get("suggestion").asText()).append(")\n");
            }
            template = template.replace("{INVALID_EXAMPLES_SECTION}", invalidExamplesBuilder.toString().trim());
            hasExample = true;
        } else {
            template = template.replace("{INVALID_EXAMPLES_SECTION}", "");
        }

        if (!hasExample) {
            template = template.replace("**Examples:**", "");
        }

        return template;
    }

    public String buildPrompt(String selectedText, String fileExtension, boolean isDefaultValidation) {
        StringBuilder inputText = new StringBuilder(buildContext(fileExtension, isDefaultValidation));

        boolean appendScenario = selectedText.contains(Constants.SCENARIO_KEYWORD);
        boolean appendGiven = selectedText.contains(Constants.GIVEN_KEYWORD);
        boolean appendWhen = selectedText.contains(Constants.WHEN_KEYWORD);
        boolean appendThen = selectedText.contains(Constants.THEN_KEYWORD);

        if (appendScenario) {
            inputText.append(buildScenarioContext());
        }
        if (appendGiven) {
            inputText.append(buildGivenContext());
        }
        if (appendWhen) {
            inputText.append(buildWhenContext());
        }
        if (appendThen) {
            inputText.append(buildThenContext());
        }

        inputText.append(PromptTemplate.OUTPUT_FORMAT_JSON).append(String.format(PromptTemplate.LLM_INPUT, selectedText));

        return inputText.toString();
    }


}
