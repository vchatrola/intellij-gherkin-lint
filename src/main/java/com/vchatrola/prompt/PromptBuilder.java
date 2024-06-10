package com.vchatrola.prompt;

import com.fasterxml.jackson.databind.JsonNode;
import com.vchatrola.util.Constants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

public class PromptBuilder {

    private static final String CONTEXT_PLACEHOLDER = "{CONTEXT}";
    private static final String TASKS_PLACEHOLDER = "{TASKS}";
    private static final String SECTION_SUFFIX = "_SECTION";
    private static final String REQUIREMENTS_SECTION = "{REQUIREMENTS_SECTION}";
    private static final String VALID_EXAMPLES_SECTION = "{VALID_EXAMPLES_SECTION}";
    private static final String INVALID_EXAMPLES_SECTION = "{INVALID_EXAMPLES_SECTION}";
    private static final String FEEDBACK_SECTION = "{FEEDBACK_SECTION}";
    private static final String TENSE_SECTION = "{TENSE_SECTION}";
    private static final String ENTITIES_PLACEHOLDER = "{ENTITIES_LIST}";
    private static final String EXAMPLES_HEADER = "* **Examples:**";
    private static final String ADDITIONAL_REQUIREMENTS = "**Additional Requirements:**";
    private static final String SUGGESTIONS_HEADER = "* **Suggestions/Feedback:**";
    private static final String TENSE_HEADER_FORMAT = "%d. **Tense:**\n%s* Ensure that %s statements are in the %s\n";
    private static final String STRUCTURE_FIELD = "structure";
    private static final String REQUIREMENTS_FIELD = "requirements";
    private static final String EXAMPLES_FIELD = "examples";
    private static final String VALID_FIELD = "valid";
    private static final String INVALID_FIELD = "invalid";
    private static final String FEEDBACK_FIELD = "feedback";
    private static final String TENSE_FIELD = "tense";

    private final JsonNode config;
    private final boolean isStory;
    private final boolean isFeature;

    public PromptBuilder(JsonNode config, String fileType) {
        this.config = config;
        this.isStory = StringUtils.equalsAnyIgnoreCase(fileType, "story");
        this.isFeature = StringUtils.equalsAnyIgnoreCase(fileType, "feature");
    }

    public String buildContext(boolean isDefaultValidation) {
        String context = PromptTemplate.CONTEXT_TEMPLATE;
        context = context.replace(CONTEXT_PLACEHOLDER, config.get("CONTEXT").asText());

        String tasks = buildTasks(isDefaultValidation);
        context = context.replace(TASKS_PLACEHOLDER, tasks.trim());

        return context;
    }

    private String buildTasks(boolean isDefaultValidation) {
        StringBuilder tasksBuilder = new StringBuilder();
        int taskNumber = 1;

        for (JsonNode task : config.get("TASKS")) {
            tasksBuilder.append(taskNumber++).append(". ").append(task.asText()).append("\n");
        }

        JsonNode entitiesNode = config.get("ENTITIES");
        if (hasEntities(entitiesNode)) {
            String entitiesTask = buildEntitiesTask(entitiesNode);
            tasksBuilder.append(taskNumber++).append(". ").append(entitiesTask).append("\n");
        }

        if (isDefaultValidation) {
            String genericPrompt = getGenericPrompt();
            tasksBuilder.append(taskNumber++).append(". ").append(genericPrompt).append("\n");
        }

        return tasksBuilder.toString();
    }

    private boolean hasEntities(JsonNode entitiesNode) {
        return entitiesNode != null && entitiesNode.isArray() && !entitiesNode.isEmpty();
    }

    private String buildEntitiesTask(JsonNode entitiesNode) {
        StringBuilder entitiesListBuilder = new StringBuilder();
        for (JsonNode entity : entitiesNode) {
            entitiesListBuilder.append(entity.asText()).append(", ");
        }
        String entitiesList = entitiesListBuilder.toString().replaceAll(", $", "");
        return PromptTemplate.ENTITIES_TASK.replace(ENTITIES_PLACEHOLDER, entitiesList);
    }

    private String getGenericPrompt() {
        if (isFeature) {
            return PromptTemplate.GENERIC_PROMPT_CUCUMBER;
        } else if (isStory) {
            return PromptTemplate.GENERIC_PROMPT_JBEHAVE;
        } else {
            return PromptTemplate.GENERIC_PROMPT_BOTH;
        }
    }

    private String buildGeneralRequirements(String selectedText) {
        String requirementsTemplate = PromptTemplate.GENERAL_REQUIREMENTS_TEMPLATE;
        StringBuilder requirementsBuilder = new StringBuilder();
        int reqNumber = 1;

        for (JsonNode requirement : config.get("REQUIREMENTS")) {
            requirementsBuilder.append(reqNumber++).append(". ").append(requirement.asText()).append("\n");
        }

        JsonNode perspectiveNode = config.get("PERSPECTIVE");
        if (hasPerspective(perspectiveNode)) {
            String perspectiveReq = String.format(PromptTemplate.PERSPECTIVE_REQUIREMENT, perspectiveNode.asText());
            requirementsBuilder.append(reqNumber++).append(". ").append(perspectiveReq).append("\n");
        }

        addConditionalRequirements(requirementsBuilder, selectedText, reqNumber);

        return requirementsTemplate.replace(REQUIREMENTS_SECTION, requirementsBuilder.toString().trim());
    }

    private boolean hasPerspective(JsonNode perspectiveNode) {
        return perspectiveNode != null && !perspectiveNode.asText().isBlank();
    }

    private void addConditionalRequirements(StringBuilder requirementsBuilder, String selectedText, int reqNumber) {
        if (selectedText.contains(Constants.BUT_KEYWORD)) {
            requirementsBuilder.append(reqNumber++).append(". ").append(PromptTemplate.BUT_REQUIREMENT).append("\n");
        }

        if (PromptUtils.hasAngleBracketPlaceholders(selectedText)) {
            requirementsBuilder.append(reqNumber++).append(". ").append(PromptTemplate.EXAMPLES_REQUIREMENT).append("\n");
        }

    }

    public String buildScenarioContext() {
        String scenarioTemplate = PromptTemplate.SCENARIO_TEMPLATE;
        JsonNode scenarioNode = config.get("SCENARIO");
        int sectionNumber = 3; // Starting from 3 because the first two requirements are fixed

        String sectionHeader = String.format(PromptTemplate.getStructureInstructions(), Constants.SCENARIO_KEYWORD);
        String indentation = PromptUtils.getLastLineIndentation(sectionHeader) + PromptUtils.generateSpaces(4);
        Pair<String, Boolean> result = replacePlaceholder(scenarioTemplate, scenarioNode, STRUCTURE_FIELD, sectionHeader,
                indentation, sectionNumber);
        scenarioTemplate = result.getLeft();
        if (result.getRight()) {
            sectionNumber++;
        }

        indentation = PromptUtils.getIndentation(scenarioTemplate, REQUIREMENTS_SECTION) + PromptUtils.generateSpaces(4);
        scenarioTemplate = replacePlaceholder(scenarioTemplate, scenarioNode, REQUIREMENTS_FIELD,
                ADDITIONAL_REQUIREMENTS, indentation, sectionNumber).getLeft();
        scenarioTemplate = replaceExamples(scenarioTemplate, scenarioNode);
        return PromptUtils.removeEmptyLines(scenarioTemplate);
    }

    public String buildGivenContext() {
        String givenTemplate = PromptTemplate.GIVEN_TEMPLATE;
        JsonNode givenNode = config.get("GIVEN");
        int sectionNumber = 2; // Starting from 2 because the first requirement is fixed

        String sectionHeader = String.format(PromptTemplate.getStructureInstructions(), Constants.GIVEN_KEYWORD);
        String indentation = PromptUtils.getLastLineIndentation(sectionHeader) + PromptUtils.generateSpaces(4);
        Pair<String, Boolean> result = replacePlaceholder(givenTemplate, givenNode, STRUCTURE_FIELD, sectionHeader,
                indentation, sectionNumber);
        givenTemplate = result.getLeft();
        if (result.getRight()) {
            sectionNumber++;
        }

        givenTemplate = replaceTenseSection(givenTemplate, givenNode, Constants.GIVEN_KEYWORD, sectionNumber++);

        indentation = PromptUtils.getIndentation(givenTemplate, REQUIREMENTS_SECTION) + PromptUtils.generateSpaces(4);
        givenTemplate = replacePlaceholder(givenTemplate, givenNode, REQUIREMENTS_FIELD,
                ADDITIONAL_REQUIREMENTS, indentation, sectionNumber).getLeft();

        givenTemplate = replaceExamples(givenTemplate, givenNode);

        // Starting from 2 because the first feedback is fixed
        givenTemplate = appendFeedbackSection(givenTemplate, givenNode, 2);

        return PromptUtils.removeEmptyLines(givenTemplate);
    }

    public String buildWhenContext() {
        String whenTemplate = PromptTemplate.WHEN_TEMPLATE;
        JsonNode whenNode = config.get("WHEN");
        int sectionNumber = 2; // Starting from 2 because the first requirement is fixed

        String sectionHeader = String.format(PromptTemplate.getStructureInstructions(), Constants.WHEN_KEYWORD);
        String indentation = PromptUtils.getLastLineIndentation(sectionHeader)
                + PromptUtils.generateSpaces(4);
        Pair<String, Boolean> result = replacePlaceholder(whenTemplate, whenNode, STRUCTURE_FIELD, sectionHeader,
                indentation, sectionNumber);
        whenTemplate = result.getLeft();
        if (result.getRight()) {
            sectionNumber++;
        }

        whenTemplate = replaceTenseSection(whenTemplate, whenNode, Constants.WHEN_KEYWORD, sectionNumber++);

        indentation = PromptUtils.getIndentation(whenTemplate, REQUIREMENTS_SECTION)
                + PromptUtils.generateSpaces(4);
        whenTemplate = replacePlaceholder(whenTemplate, whenNode, REQUIREMENTS_FIELD,
                ADDITIONAL_REQUIREMENTS, indentation, sectionNumber).getLeft();

        whenTemplate = replaceExamples(whenTemplate, whenNode);

        // Starting from 2 because the first feedback is fixed
        whenTemplate = appendFeedbackSection(whenTemplate, whenNode, 2);

        return PromptUtils.removeEmptyLines(whenTemplate);
    }

    public String buildThenContext() {
        String thenTemplate = PromptTemplate.THEN_TEMPLATE;
        JsonNode thenNode = config.get("THEN");
        int sectionNumber = 3; // // Starting from 3 because the first two requirements are fixed

        String sectionHeader = String.format(PromptTemplate.getStructureInstructions(), Constants.THEN_KEYWORD);
        String indentation = PromptUtils.getLastLineIndentation(sectionHeader)
                + PromptUtils.generateSpaces(4);
        Pair<String, Boolean> result = replacePlaceholder(thenTemplate, thenNode, STRUCTURE_FIELD, sectionHeader,
                indentation, sectionNumber);
        thenTemplate = result.getLeft();
        if (result.getRight()) {
            sectionNumber++;
        }

        thenTemplate = replaceTenseSection(thenTemplate, thenNode, Constants.THEN_KEYWORD, sectionNumber++);

        indentation = PromptUtils.getIndentation(thenTemplate, REQUIREMENTS_SECTION)
                + PromptUtils.generateSpaces(4);
        thenTemplate = replacePlaceholder(thenTemplate, thenNode, REQUIREMENTS_FIELD,
                ADDITIONAL_REQUIREMENTS, indentation, sectionNumber).getLeft();

        thenTemplate = replaceExamples(thenTemplate, thenNode);

        thenTemplate = appendFeedbackSection(thenTemplate, thenNode, 1);

        return PromptUtils.removeEmptyLines(thenTemplate);
    }

    private String buildTagContext() {
        String tagTemplate = PromptTemplate.TAG_TEMPLATE;
        JsonNode tagNode = config.get("TAG");
        tagTemplate = replacePlaceholder(tagTemplate, tagNode, REQUIREMENTS_FIELD, "", "",
                1).getLeft();
        return tagTemplate;
    }

    private Pair<String, Boolean> replacePlaceholder(String template, JsonNode node, String sectionKey, String sectionHeader,
                                                     String indentation, int sectionNumber) {
        if (node.hasNonNull(sectionKey) && node.get(sectionKey).isArray() && !node.get(sectionKey).isEmpty()) {
            StringBuilder sectionBuilder = new StringBuilder();
            if (!sectionHeader.isBlank()) {
                sectionBuilder.append(sectionNumber).append(". ").append(sectionHeader).append("\n");
            }
            for (JsonNode element : node.get(sectionKey)) {
                sectionBuilder.append(indentation).append("* \"").append(element.asText()).append("\"\n");
            }
            String updatedTemplate = template.replace("{" + sectionKey.toUpperCase() + SECTION_SUFFIX + "}", sectionBuilder.toString().trim());
            return Pair.of(updatedTemplate, true); // Section added
        } else {
            String updatedTemplate = template.replace("{" + sectionKey.toUpperCase() + SECTION_SUFFIX + "}", "");
            return Pair.of(updatedTemplate, false); // No section added
        }
    }

    private String replaceExamples(String template, JsonNode parentNode) {
        boolean hasExample = false;

        if (!parentNode.hasNonNull(EXAMPLES_FIELD)) {
            return template.replace(EXAMPLES_HEADER, "");
        }
        JsonNode examplesNode = parentNode.get(EXAMPLES_FIELD);

        if (examplesNode.hasNonNull(VALID_FIELD) && examplesNode.get(VALID_FIELD).isArray() && !examplesNode.get(VALID_FIELD).isEmpty()) {
            StringBuilder validExamplesBuilder = new StringBuilder("* **Good Example:**\n");
            String indentation = PromptUtils.getIndentation(template, VALID_EXAMPLES_SECTION) + PromptUtils.generateSpaces(4);
            int number = 1;
            for (JsonNode validExample : examplesNode.get(VALID_FIELD)) {
                validExamplesBuilder.append(indentation).append(number++).append(". ").append(validExample.asText()).append("\n");
            }
            template = template.replace(VALID_EXAMPLES_SECTION, validExamplesBuilder.toString().trim());
            hasExample = true;
        } else {
            template = template.replace(VALID_EXAMPLES_SECTION, "");
        }

        if (examplesNode.hasNonNull(INVALID_FIELD) && examplesNode.get(INVALID_FIELD).isArray() && !examplesNode.get(INVALID_FIELD).isEmpty()) {
            StringBuilder invalidExamplesBuilder = new StringBuilder("* **Bad Example:**\n");
            String indentation = PromptUtils.getIndentation(template, INVALID_EXAMPLES_SECTION) + PromptUtils.generateSpaces(4);
            int number = 1;
            for (JsonNode invalidExample : examplesNode.get(INVALID_FIELD)) {
                invalidExamplesBuilder.append(indentation).append(number++).append(". ").append(invalidExample.get("example").asText())
                        .append(" [Reason: ").append(invalidExample.get("reason").asText())
                        .append(", Suggestion: ").append(invalidExample.get("suggestion").asText()).append("]\n");
            }
            template = template.replace(INVALID_EXAMPLES_SECTION, invalidExamplesBuilder.toString().trim());
            hasExample = true;
        } else {
            template = template.replace(INVALID_EXAMPLES_SECTION, "");
        }

        if (!hasExample) {
            template = template.replace(EXAMPLES_HEADER, "");
        }

        return template;
    }

    private static String appendFeedbackSection(String template, JsonNode node, int startingNumber) {
        if (node.hasNonNull(FEEDBACK_FIELD) && node.get(FEEDBACK_FIELD).isArray() && !node.get(FEEDBACK_FIELD).isEmpty()) {
            StringBuilder feedbackBuilder = new StringBuilder();
            int feedbackNumber = startingNumber;
            String indentation = PromptUtils.getIndentation(template, FEEDBACK_SECTION);

            for (JsonNode feedback : node.get(FEEDBACK_FIELD)) {
                feedbackBuilder.append(String.format("%s%d. %s%n", indentation, feedbackNumber++, feedback.asText()));
            }
            return template.replace(FEEDBACK_SECTION, feedbackBuilder.toString().trim());
        } else {
            if (startingNumber == 1) {
                template = template.replace(SUGGESTIONS_HEADER, "");
            }
            return template.replace(FEEDBACK_SECTION, "");
        }
    }

    private String replaceTenseSection(String template, JsonNode node, String stepKeyword, int sectionNumber) {
        if (node.hasNonNull(TENSE_FIELD) && !node.get(TENSE_FIELD).asText().isBlank()) {
            String tenseSection = String.format(TENSE_HEADER_FORMAT, sectionNumber, PromptUtils.generateSpaces(12),
                    stepKeyword, node.get(TENSE_FIELD).asText());
            return template.replace(TENSE_SECTION, tenseSection.trim());
        } else {
            return template.replace(TENSE_SECTION, "");
        }
    }

    public String buildPrompt(String selectedText, boolean isDefaultValidation) {
        StringBuilder inputText = new StringBuilder(buildContext(isDefaultValidation))
                .append("\n");

        boolean appendScenario = selectedText.contains(Constants.SCENARIO_KEYWORD);
        boolean appendGiven = selectedText.contains(Constants.GIVEN_KEYWORD);
        boolean appendWhen = selectedText.contains(Constants.WHEN_KEYWORD);
        boolean appendThen = selectedText.contains(Constants.THEN_KEYWORD);

        if (appendScenario || appendGiven || appendWhen || appendThen) {
            inputText.append(buildGeneralRequirements(selectedText)).append("\n");
        }
        if (appendScenario) {
            inputText.append(buildScenarioContext()).append("\n");
        }
        if (appendGiven) {
            inputText.append(buildGivenContext()).append("\n");
        }
        if (appendWhen) {
            inputText.append(buildWhenContext()).append("\n");
        }
        if (appendThen) {
            inputText.append(buildThenContext()).append("\n");
        }
        if (hasGherkinTags(selectedText)) {
            inputText.append(buildTagContext()).append("\n");
        }

        inputText.append(PromptTemplate.OUTPUT_FORMAT_JSON).append(String.format(PromptTemplate.LLM_INPUT, selectedText));

        return inputText.toString();
    }

    private boolean hasGherkinTags(String text) {
        return StringUtils.containsAnyIgnoreCase(text, "Meta")
                || StringUtils.containsAnyIgnoreCase(text, "@");
    }

}
