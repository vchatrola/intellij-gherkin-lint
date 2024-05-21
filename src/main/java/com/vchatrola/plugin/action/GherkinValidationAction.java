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
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.vchatrola.gemini.service.GeminiService;
import com.vchatrola.gemini.util.Prompts;
import com.vchatrola.plugin.service.MyPluginServiceImpl;
import com.vchatrola.plugin.util.PluginConstants;
import com.vchatrola.plugin.util.PluginUtils;
import org.jetbrains.annotations.NotNull;

public class GherkinValidationAction extends AnAction {


    @Override
    public void actionPerformed(AnActionEvent event) {
        Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
        String selectedText = PluginUtils.getSelectedText(editor);

        ToolWindow toolWindow = PluginUtils.getToolWindow(event, PluginConstants.TOOL_WINDOW_ID);
        if (toolWindow == null) {
            return;
        }
        toolWindow.setAutoHide(false);

        ConsoleView consoleView = PluginUtils.createConsoleView(event);
        if (consoleView == null) {
            return;
        }

        Content content = PluginUtils.createContent(toolWindow, consoleView, PluginConstants.CONTENT_DISPLAY_NAME);
        validateGherkinText(selectedText, consoleView);
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
        if (psiFile == null || !caret.hasSelection()) {
            return false;
        }

        String fileExtension = psiFile.getFileType().getDefaultExtension();
        if (!PluginConstants.SUPPORTED_FILE_EXTENSIONS.contains(fileExtension)) {
            return false;
        }

        String selectedText = caret.getSelectedText();
        if (selectedText == null || selectedText.trim().split("\\s+").length < 3) {
            return false;
        }

        String firstLine = selectedText.trim().split("\\R")[0].trim();
        String firstWord = firstLine.split("\\s+")[0];
        return PluginConstants.GHERKIN_KEYWORDS.contains(firstWord);
    }

    private void validateGherkinText(String selectedText, ConsoleView consoleView) {
        if (selectedText == null || selectedText.trim().isEmpty()) {
            consoleView.print("No Gherkin text selected. Please select Gherkin text to validate.\n",
                    ConsoleViewContentType.ERROR_OUTPUT);
            return;
        }

        MyPluginServiceImpl myPluginServiceImpl = ApplicationManager.getApplication().getService(MyPluginServiceImpl.class);
        GeminiService service = myPluginServiceImpl.getGeminiService();
        if (service == null) {
            consoleView.print("GeminiService not available.\n", ConsoleViewContentType.ERROR_OUTPUT);
            return;
        }

        String textToSend = Prompts.GHERKIN_VALIDATION + "\n" + selectedText;
        String response = service.getCompletion(textToSend);
        if (response != null) {
            consoleView.print(response, ConsoleViewContentType.NORMAL_OUTPUT);
        } else {
            consoleView.print("No response received from the Gemini service.\n", ConsoleViewContentType.ERROR_OUTPUT);
        }
    }

}
