package com.vchatrola.plugin.util;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.vchatrola.util.GherkinLintLogger;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * Utility class for plugin-related functionalities.
 */

public class PluginUtils {

    public static String getSelectedText(Editor editor) {
        return editor.getCaretModel().getCurrentCaret().getSelectedText();
    }

    @Nullable
    public static ToolWindow getToolWindow(AnActionEvent event, String toolWindowId) {
        return ToolWindowManager.getInstance(Objects.requireNonNull(event.getProject()))
                .getToolWindow(toolWindowId);
    }

    @Nullable
    public static ConsoleView createConsoleView(AnActionEvent event) {
        try {
            return TextConsoleBuilderFactory.getInstance()
                    .createBuilder(Objects.requireNonNull(event.getProject())).getConsole();
        } catch (Exception e) {
            GherkinLintLogger.error("Failed to create console view.", e);
            return null;
        }
    }

    public static Content createToolWindowContent(ToolWindow toolWindow, ConsoleView consoleView, String displayName) {
        try {
            String contentName = generateContentName(toolWindow, displayName);
            Content content = ContentFactory.getInstance().createContent(consoleView.getComponent(), contentName, false);
            content.setCloseable(true);
            toolWindow.getContentManager().addContent(content);
            GherkinLintLogger.info("Content created and added to tool window with name: " + contentName);
            return content;
        } catch (Exception e) {
            GherkinLintLogger.error("Failed to create content for tool window.", e);
            return null;
        }
    }

    private static String generateContentName(ToolWindow toolWindow, String displayName) {
        int contentCount = toolWindow.getContentManager().getContentCount();
        return displayName + (contentCount >= 1 ? " (" + (contentCount + 1) + ")" : "");
    }

    public static String getFirstWordOnlyAlphabets(String text) {
        String[] words = text.split("\\s+");
        if (words.length > 0) {
            return words[0].replaceAll("[^a-zA-Z]", "");
        }
        return "";
    }

}
