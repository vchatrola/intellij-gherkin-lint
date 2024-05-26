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
        GherkinLintLogger.info("Gherkin text validated and ToolWindow content activated.");
    }

    @Override
    public void update(AnActionEvent event) {
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        Caret caret = editor.getCaretModel().getCurrentCaret();
        PsiFile psiFile = event.getData(CommonDataKeys.PSI_FILE);

        // Enable the action if the validation is applicable
        boolean isApplicable = isValidationApplicable(caret, psiFile);
        event.getPresentation().setEnabledAndVisible(isApplicable);
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
        if (!PluginConstants.SUPPORTED_FILE_EXTENSIONS.contains(fileExtension)) {
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
                try {
                    indicator.setText("Validating Gherkin...");

                    GeminiService service = getGeminiService(consoleView);
                    String response = service.getCompletion(prompt);
                    if (StringUtils.isBlank(response)) {
                        GherkinLintLogger.error(PluginConstants.ERROR_MESSAGE_NO_RESPONSE);
                        consoleView.print(PluginConstants.ERROR_MESSAGE_NO_RESPONSE, ConsoleViewContentType.ERROR_OUTPUT);
                        return;
                    }

                    // Update UI thread with the response (using SwingUtilities.invokeLater)
                    String finalResponse = GherkinOutputParser.parseOutput(response);
                    SwingUtilities.invokeLater(() -> {
                        consoleView.print(finalResponse, ConsoleViewContentType.NORMAL_OUTPUT);
                        indicator.setText("Validation complete"); //TODO - DEBUG
                    });
                } catch (Exception ex) {
                    GherkinLintLogger.error(PluginConstants.ERROR_MESSAGE_VALIDATION, ex);
                    consoleView.print(PluginConstants.ERROR_MESSAGE_VALIDATION, ConsoleViewContentType.ERROR_OUTPUT);
                }
            }
        });
    }

    private boolean isEmptyOrInvalidText(String text, ConsoleView consoleView) {
        if (text == null || text.trim().isEmpty()) {
            consoleView.print(PluginConstants.ERROR_MESSAGE_NO_GHERKIN_TEXT_SELECTED + "\n", ConsoleViewContentType.ERROR_OUTPUT);
            return true;
        }
        return false;
    }

    private boolean isTooShort(String text, ConsoleView consoleView) {
        if (text.trim().split("\\s+").length < 3) {
            consoleView.print(PluginConstants.ERROR_MESSAGE_GHERKIN_TEXT_TOO_SHORT + "\n", ConsoleViewContentType.ERROR_OUTPUT);
            return true;
        }
        return false;
    }

    private boolean startsWithAndKeyword(String text, ConsoleView consoleView) {
        if (StringUtils.equalsAnyIgnoreCase(PluginConstants.KEYWORD_AND, PluginUtils.getFirstWordOnlyAlphabets(text))) {
            consoleView.print(PluginConstants.ERROR_MESSAGE_GHERKIN_AND_NO_CONTEXT + "\n", ConsoleViewContentType.ERROR_OUTPUT);
            return true;
        }
        return false;
    }

    private GeminiService getGeminiService(ConsoleView consoleView) {
        MyPluginServiceImpl myPluginServiceImpl = ApplicationManager.getApplication().getService(MyPluginServiceImpl.class);
        GeminiService service = myPluginServiceImpl.getGeminiService();
        if (service == null) {
            consoleView.print("Error: Unable to access GeminiService. Please ensure the service is properly " +
                    "configured and try again.\n", ConsoleViewContentType.ERROR_OUTPUT);
        }
        return service;
    }

    private String buildPrompt(String selectedText) {
        StringBuilder inputText = new StringBuilder(Prompts.CONTEXT);
        if (selectedText.contains(PluginConstants.KEYWORD_SCENARIO)) {
            inputText.append(Prompts.SCENARIO);
        }
        if (selectedText.contains(PluginConstants.KEYWORD_GIVEN)) {
            inputText.append(Prompts.GIVEN);
        }
        if (selectedText.contains(PluginConstants.KEYWORD_WHEN)) {
            inputText.append(Prompts.WHEN);
        }
        if (selectedText.contains(PluginConstants.KEYWORD_THEN)) {
            inputText.append(Prompts.THEN);
        }
        inputText.append(Prompts.OUTPUT_FORMAT_JSON).append(String.format(Prompts.LLM_INPUT, selectedText));
        return inputText.toString();
    }

}
