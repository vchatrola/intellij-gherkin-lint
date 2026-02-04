package com.vchatrola.plugin.util;

import com.intellij.execution.filters.TextConsoleBuilder;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.impl.ConsoleViewImpl;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.vchatrola.util.GherkinLintLogger;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/** Utility class for plugin-related functionalities. */
public class PluginUtils {
  private static int contentSequence = 0;

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
      TextConsoleBuilder builder =
          TextConsoleBuilderFactory.getInstance()
              .createBuilder(Objects.requireNonNull(event.getProject()));
      ConsoleView consoleView = builder.getConsole();
      if (consoleView instanceof ConsoleViewImpl consoleViewImpl) {
        enableSoftWraps(consoleViewImpl);
      }
      return consoleView;
    } catch (Exception e) {
      GherkinLintLogger.error("Failed to create console view.", e);
      return null;
    }
  }

  public static Content createToolWindowContent(
      ToolWindow toolWindow, ConsoleView consoleView, String displayName) {
    try {
      if (getExistingResultCount(toolWindow, displayName) == 0) {
        contentSequence = 0;
      }
      String contentName = generateContentName(toolWindow, displayName);
      Content content =
          ContentFactory.getInstance()
              .createContent(consoleView.getComponent(), contentName, false);
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
    int next = ++contentSequence;
    return displayName + (next > 1 ? " (" + next + ")" : "");
  }

  private static int getExistingResultCount(ToolWindow toolWindow, String displayName) {
    int count = 0;
    for (Content content : toolWindow.getContentManager().getContents()) {
      String name = content.getDisplayName();
      if (name != null && name.startsWith(displayName)) {
        count++;
      }
    }
    return count;
  }

  public static String getFirstKeywordToken(String text) {
    if (text == null || text.isBlank()) {
      return "";
    }

    String trimmed = text.trim();
    if (trimmed.startsWith("@")) {
      return "@";
    }
    if (trimmed.startsWith("*")) {
      return "*";
    }

    String[] words = trimmed.split("\\s+");
    if (words.length > 0) {
      return words[0].replaceAll("[^a-zA-Z]", "");
    }
    return "";
  }

  private static void enableSoftWraps(ConsoleViewImpl consoleView) {
    if (consoleView.getEditor() != null) {
      consoleView.getEditor().getSettings().setUseSoftWraps(true);
      return;
    }
    com.intellij.openapi.application.ApplicationManager.getApplication()
        .invokeLater(
            () -> {
              if (consoleView.getEditor() != null) {
                consoleView.getEditor().getSettings().setUseSoftWraps(true);
              } else {
                GherkinLintLogger.warn("ConsoleView editor unavailable; soft wraps not enabled.");
              }
            });
  }
}
