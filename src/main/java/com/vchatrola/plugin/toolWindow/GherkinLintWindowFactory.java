package com.vchatrola.plugin.toolWindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.vchatrola.common.GherkinLintLogger;
import com.vchatrola.plugin.util.PluginConstants;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class GherkinLintWindowFactory implements ToolWindowFactory {


    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        try {
            // Ensure this code runs on the Event Dispatch Thread
            JPanel contentPanel = new JPanel(new BorderLayout());
            JLabel label = new JLabel(PluginConstants.TOOL_WINDOW_DEFAULT_MESSAGE);
            label.setForeground(UIManager.getColor("textInactiveText")); // Set text color to lighter shade
            contentPanel.add(label, BorderLayout.CENTER);
            toolWindow.getComponent().add(contentPanel);
            GherkinLintLogger.info("Tool window content created successfully.");
        } catch (Exception e) {
            GherkinLintLogger.error("Failed to create tool window content.", e);
        }
    }

}