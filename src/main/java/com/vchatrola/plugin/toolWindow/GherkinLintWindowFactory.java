package com.vchatrola.plugin.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.vchatrola.util.Constants;
import com.vchatrola.util.GherkinLintLogger;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import org.jetbrains.annotations.NotNull;

public class GherkinLintWindowFactory implements ToolWindowFactory {

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    try {
      if (toolWindow.getContentManager().getContentCount() > 0) {
        return;
      }
      JPanel contentPanel = new JPanel(new GridBagLayout());
      JLabel label = new JLabel(buildWelcomeHtml());
      label.setForeground(UIManager.getColor("textInactiveText"));
      contentPanel.add(label, new GridBagConstraints());

      Content content = ContentFactory.getInstance().createContent(contentPanel, "Welcome", false);
      content.setCloseable(false);
      toolWindow.getContentManager().addContent(content);
      GherkinLintLogger.info("Tool window content created successfully.");
    } catch (Exception e) {
      GherkinLintLogger.error("Failed to create tool window content.", e);
    }
  }

  private static String buildWelcomeHtml() {
    String extensions = String.join(", ", Constants.SUPPORTED_EXTENSIONS);
    return "<html>"
        + "<b>GherkinLint</b><br/>"
        + "Select Gherkin text in a "
        + extensions
        + " file.<br/>"
        + "Right-click &rarr; <i>Validate Gherkin</i>.<br/>"
        + "Results appear here.<br/><br/>"
        + "<span style='color:gray'>Privacy: selected text is sent to Gemini.</span>"
        + "</html>";
  }
}
