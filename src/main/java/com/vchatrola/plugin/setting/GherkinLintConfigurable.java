package com.vchatrola.plugin.setting;

import com.intellij.openapi.options.Configurable;
import com.vchatrola.util.GherkinLintLogger;
import javax.swing.JComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

public class GherkinLintConfigurable implements Configurable {
  private GherkinLintSettingsUI gherkinLintSettingsUI;

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "GherkinLint";
  }

  @Nullable
  @Override
  public JComponent createComponent() {
    gherkinLintSettingsUI = new GherkinLintSettingsUI();
    reset();
    return gherkinLintSettingsUI.createPanel();
  }

  @Override
  public boolean isModified() {
    if (gherkinLintSettingsUI == null) {
      return false;
    }
    GherkinLintSettingsState settings = GherkinLintSettingsState.getInstance();
    String apiKey = gherkinLintSettingsUI.getApiKey();
    return settings.isCustomLogicEnabled() != gherkinLintSettingsUI.isCustomLogicEnabled()
        || !settings.getCustomFilePath().equals(gherkinLintSettingsUI.getCustomFilePath())
        || !settings.getCopyDirectoryPath().equals(gherkinLintSettingsUI.getCopyDirectoryPath())
        || !settings.getGeminiModel().equals(gherkinLintSettingsUI.getGeminiModel())
        || settings.isVerboseLogging() != gherkinLintSettingsUI.isVerboseLoggingEnabled()
        || !apiKey.isEmpty();
  }

  @Override
  public void apply() {
    if (gherkinLintSettingsUI == null) {
      return;
    }
    GherkinLintSettingsState settings = GherkinLintSettingsState.getInstance();
    boolean previousCustomEnabled = settings.customLogicEnabled;
    String previousCustomFile = settings.customFilePath;
    boolean customEnabled = gherkinLintSettingsUI.isCustomLogicEnabled();
    if (customEnabled && gherkinLintSettingsUI.getCustomFilePath().trim().isEmpty()) {
      customEnabled = false;
    }
    settings.customLogicEnabled = customEnabled;
    settings.verboseLogging = gherkinLintSettingsUI.isVerboseLoggingEnabled();
    GherkinLintLogger.setVerboseEnabled(settings.verboseLogging);
    settings.customFilePath = gherkinLintSettingsUI.getCustomFilePath();
    settings.copyDirectoryPath = gherkinLintSettingsUI.getCopyDirectoryPath();
    settings.geminiModel = gherkinLintSettingsUI.getGeminiModel();
    String apiKey = gherkinLintSettingsUI.getApiKey();
    if (!apiKey.isEmpty()) {
      com.intellij.openapi.application.ApplicationManager.getApplication()
          .executeOnPooledThread(
              () -> {
                GherkinLintSecrets.saveApiKey(apiKey);
                com.vchatrola.gemini.service.GeminiService.clearCachedModels();
                javax.swing.SwingUtilities.invokeLater(
                    () -> {
                      if (gherkinLintSettingsUI != null) {
                        gherkinLintSettingsUI.resetApiKeyField();
                      }
                    });
              });
    } else {
      gherkinLintSettingsUI.resetApiKeyField();
    }
    if (previousCustomEnabled != customEnabled
        || !previousCustomFile.equals(settings.customFilePath)) {
      com.vchatrola.config.ConfigurationManager.invalidateCache();
    }
    gherkinLintSettingsUI.setCustomLogicEnabled(customEnabled);
    gherkinLintSettingsUI.updateCustomRulesWarning();
  }

  @Override
  public void reset() {
    if (gherkinLintSettingsUI == null) {
      return;
    }
    GherkinLintSettingsState settings = GherkinLintSettingsState.getInstance();
    gherkinLintSettingsUI.setCustomLogicEnabled(settings.customLogicEnabled);
    gherkinLintSettingsUI.setVerboseLoggingEnabled(settings.verboseLogging);
    GherkinLintLogger.setVerboseEnabled(settings.verboseLogging);
    gherkinLintSettingsUI.setCustomFilePath(settings.customFilePath);
    gherkinLintSettingsUI.setCopyDirectoryPath(settings.copyDirectoryPath);
    gherkinLintSettingsUI.setGeminiModel(settings.geminiModel);
    gherkinLintSettingsUI.resetApiKeyField();
  }

  @Override
  public void disposeUIResources() {
    gherkinLintSettingsUI = null;
  }
}
