package com.vchatrola.plugin.setting;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

@State(
    name = "com.vchatrola.plugin.setting.GherkinLintSettingsState",
    storages = @Storage("GherkinLintSettings.xml"))
@Service(Service.Level.APP)
public final class GherkinLintSettingsState
    implements PersistentStateComponent<GherkinLintSettingsState> {
  public boolean customLogicEnabled = false;
  public String customFilePath = "";
  public String copyDirectoryPath = "";
  public String geminiModel = "";
  public List<String> geminiModels = new ArrayList<>();
  public long geminiModelsFetchedAt = 0L;

  @Override
  public @NotNull GherkinLintSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(GherkinLintSettingsState state) {
    this.customLogicEnabled = state.customLogicEnabled;
    this.customFilePath = state.customFilePath;
    this.copyDirectoryPath = state.copyDirectoryPath;
    this.geminiModel = state.geminiModel;
    this.geminiModels =
        state.geminiModels != null ? new ArrayList<>(state.geminiModels) : new ArrayList<>();
    this.geminiModelsFetchedAt = state.geminiModelsFetchedAt;
  }

  public static GherkinLintSettingsState getInstance() {
    return com.intellij.openapi.application.ApplicationManager.getApplication()
        .getService(GherkinLintSettingsState.class);
  }

  public String getCustomFilePath() {
    return customFilePath;
  }

  public String getCopyDirectoryPath() {
    return copyDirectoryPath;
  }

  public String getGeminiModel() {
    return geminiModel;
  }

  public List<String> getGeminiModels() {
    return geminiModels;
  }

  public long getGeminiModelsFetchedAt() {
    return geminiModelsFetchedAt;
  }

  public boolean isCustomLogicEnabled() {
    return customLogicEnabled;
  }
}
