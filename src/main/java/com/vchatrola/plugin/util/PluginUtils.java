package com.vchatrola.plugin.util;

import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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
            e.printStackTrace();
            return null;
        }
    }

    public static Content createContent(ToolWindow toolWindow, ConsoleView consoleView, String displayName) {
        String contentName = determineContentName(toolWindow, displayName);
        Content content = ContentFactory.getInstance().createContent(consoleView.getComponent(), contentName, false);
        content.setCloseable(true);
        toolWindow.getContentManager().addContent(content);
        return content;
    }

    private static String determineContentName(ToolWindow toolWindow, String displayName) {
        int contentCount = toolWindow.getContentManager().getContentCount();
        return displayName + (contentCount > 1 ? " (" + contentCount + ")" : "");
    }

}
