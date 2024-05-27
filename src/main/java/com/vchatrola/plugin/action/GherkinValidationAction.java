package com.vchatrola.plugin.action;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.vchatrola.common.GherkinLintLogger;
import com.vchatrola.gemini.service.GeminiService;
import com.vchatrola.gemini.util.GherkinOutputParser;
import com.vchatrola.gemini.util.Prompts;
import com.vchatrola.plugin.service.MyPluginServiceImpl;
import com.vchatrola.plugin.util.PluginConstants;
import com.vchatrola.plugin.util.PluginUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class GherkinValidationAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        String selectedText = PluginUtils.getSelectedText(editor);

        ToolWindow toolWindow = PluginUtils.getToolWindow(event, PluginConstants.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            GherkinLintLogger.error("ToolWindow with ID " + PluginConstants.TOOL_WINDOW_ID + " not found.");
            return;
        }
        toolWindow.setAutoHide(false);

        ConsoleView consoleView = PluginUtils.createConsoleView(event);
        if (consoleView == null) {
            GherkinLintLogger.error("Failed to create ConsoleView.");
            return;
        }

        Content content = PluginUtils.createToolWindowContent(toolWindow, consoleView, PluginConstants.CONTENT_DISPLAY_NAME);
        validateGherkinText(selectedText, consoleView, event.getProject());
        toolWindow.getContentManager().setSelectedContent(content);
        toolWindow.activate(null);
    }

    @Override
    public void update(AnActionEvent event) {
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        Caret caret = editor.getCaretModel().getCurrentCaret();
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

        // Enable the action if the validation is applicable
        event.getPresentation().setEnabledAndVisible(isValidationApplicable(caret, psiFile));
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }

    private boolean isValidationApplicable(Caret caret, PsiFile psiFile) {
        if (psiFile == null || caret == null || !caret.hasSelection()) {
            return false;
        }

        String fileExtension = psiFile.getFileType().getDefaultExtension();
        if (!PluginConstants.SUPPORTED_EXTENSIONS.contains(fileExtension)) {
            GherkinLintLogger.info("Validation is not applicable: Unsupported file extension: " + fileExtension);
            return false;
        }

        String selectedText = caret.getSelectedText();
        String firstLine = selectedText.trim().split("\\R")[0].trim();
        String firstWord = PluginUtils.getFirstWordOnlyAlphabets(firstLine);

        if (!PluginConstants.GHERKIN_KEYWORDS.contains(firstWord)) {
            GherkinLintLogger.info("Validation is not applicable: First word is not a Gherkin keyword: " + firstWord);
            return false;
        }

        return true;
    }

    private void validateGherkinText(String selectedText, ConsoleView consoleView, @Nullable Project project) {
        if (isEmptyOrInvalidText(selectedText, consoleView)
                || isTooShort(selectedText, consoleView)
                || startsWithAndKeyword(selectedText, consoleView)) {
            return;
        }

        String prompt = buildPrompt(selectedText);
        runValidationTask(project, consoleView, prompt);
    }

    private void runValidationTask(@Nullable Project project, ConsoleView consoleView, String prompt) {
        ProgressManager.getInstance().run(new Task.Backgroundable(project, "Validating gherkin text") {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String response;
                try {
                    indicator.setText("Validating Gherkin...");

                    GeminiService service = getGeminiService();
                    if (service == null) {
                        reportError(consoleView, PluginConstants.GEMINI_SERVICE_ACCESS_ERROR, null);
                        return;
                    }

                    response = service.getCompletion(prompt);
                    GherkinLintLogger.debug("Original Gemini response: " + response);
                    if (StringUtils.isBlank(response)) {
                        reportError(consoleView, PluginConstants.NO_GEMINI_SERVICE_RESPONSE_ERROR,
                                "Gemini response is blank or null.");
                        return;
                    }

                    String finalResponse = GherkinOutputParser.parseOutput(response);
                    if (StringUtils.isBlank(finalResponse)) {
                        reportError(consoleView, PluginConstants.UNKNOWN_ERROR,
                                "Parsed Gemini response is blank or null.");
                        return;
                    }

                    handleValidResponseAsync(indicator, finalResponse, consoleView);
                } catch (Exception ex) {
                    reportError(consoleView, PluginConstants.UNKNOWN_ERROR, ex.getMessage());
                }
            }
        });
    }

    private void handleValidResponseAsync(@NotNull ProgressIndicator indicator, String finalResponse, ConsoleView consoleView) {
        SwingUtilities.invokeLater(() -> {
            GherkinLintLogger.debug("Attempting to print final response to console view.");
            try {
                printStyledOutput(finalResponse, consoleView);
                indicator.setText("Validation complete");
                GherkinLintLogger.info("Gherkin text validated and ToolWindow content activated.");
            } catch (Exception ex) {
                reportError(consoleView, PluginConstants.CONSOLE_OUTPUT_PRINT_FAILURE,
                        "Consider investigating EDT issues.");
            }
        });
    }

    private static void printStyledOutput(String output, ConsoleView consoleView) {
        String[] lines = output.split("\\n");

        for (String line : lines) {
            String[] parts = line.split("\\|", 2);
            switch (parts[0]) {
                case "Title":
                    consoleView.print(parts[1] + "\n", ConsoleViewContentType.LOG_VERBOSE_OUTPUT);
                    break;
                case "Status":
                case "Reason":
                case "Suggestion":
                    consoleView.print(String.format("- %-10s: ", parts[0]), ConsoleViewContentType.LOG_DEBUG_OUTPUT);
                    ConsoleViewContentType contentType = ("Valid").equals(parts[1]) ? ConsoleViewContentType.USER_INPUT
                            : ("Invalid").equals(parts[1]) ? ConsoleViewContentType.ERROR_OUTPUT
                            : ConsoleViewContentType.NORMAL_OUTPUT;
                    consoleView.print(parts[1] + "\n", contentType);
                    break;
                default:
                    consoleView.print(line + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
            }
        }
    }

    private void reportError(ConsoleView consoleView, String errorMessage, @Nullable String additionalInfo) {
        GherkinLintLogger.error(errorMessage + (additionalInfo != null ? ": " + additionalInfo : ""));
        consoleView.print(errorMessage, ConsoleViewContentType.ERROR_OUTPUT);
    }

    private boolean isEmptyOrInvalidText(String text, ConsoleView consoleView) {
        if (text == null || text.trim().isEmpty()) {
            reportError(consoleView, PluginConstants.NO_GHERKIN_TEXT_SELECTED_ERROR, null);
            return true;
        }
        return false;
    }

    private boolean isTooShort(String text, ConsoleView consoleView) {
        if (text.trim().split("\\s+").length < 4) {
            reportError(consoleView, PluginConstants.GHERKIN_TEXT_TOO_SHORT_ERROR, null);
            return true;
        }
        return false;
    }

    private boolean startsWithAndKeyword(String text, ConsoleView consoleView) {
        if (StringUtils.equalsAnyIgnoreCase(PluginConstants.AND_KEYWORD, PluginUtils.getFirstWordOnlyAlphabets(text))) {
            reportError(consoleView, PluginConstants.GHERKIN_AND_NO_CONTEXT_ERROR, null);
            return true;
        }
        return false;
    }

    private GeminiService getGeminiService() {
        MyPluginServiceImpl myPluginServiceImpl = ApplicationManager.getApplication().getService(MyPluginServiceImpl.class);
        return myPluginServiceImpl.getGeminiService();
    }

    private String buildPrompt(String selectedText) {
        StringBuilder inputText = new StringBuilder(Prompts.CONTEXT);
        if (selectedText.contains(PluginConstants.SCENARIO_KEYWORD)) {
            inputText.append(Prompts.SCENARIO);
        }
        if (selectedText.contains(PluginConstants.GIVEN_KEYWORD)) {
            inputText.append(Prompts.GIVEN);
        }
        if (selectedText.contains(PluginConstants.WHEN_KEYWORD)) {
            inputText.append(Prompts.WHEN);
        }
        if (selectedText.contains(PluginConstants.THEN_KEYWORD)) {
            inputText.append(Prompts.THEN);
        }
        inputText.append(Prompts.OUTPUT_FORMAT_JSON).append(String.format(Prompts.LLM_INPUT, selectedText));
        return inputText.toString();
    }

}
