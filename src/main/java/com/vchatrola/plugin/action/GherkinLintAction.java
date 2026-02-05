package com.vchatrola.plugin.action;

import com.fasterxml.jackson.databind.JsonNode;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
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
import com.vchatrola.config.ConfigurationManager;
import com.vchatrola.gemini.api.GeminiApiException;
import com.vchatrola.gemini.service.GeminiService;
import com.vchatrola.plugin.service.GherkinLintServiceImpl;
import com.vchatrola.plugin.setting.GherkinLintSettingsManager;
import com.vchatrola.plugin.util.PluginUtils;
import com.vchatrola.prompt.PromptBuilder;
import com.vchatrola.util.Constants;
import com.vchatrola.util.GherkinLintLogger;
import com.vchatrola.util.GherkinOutputParser;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GherkinLintAction extends AnAction {

  public static String fileType;
  private static volatile long lastValidationAt = 0L;

  @Override
  public void actionPerformed(AnActionEvent event) {
    Editor editor = event.getRequiredData(CommonDataKeys.EDITOR);
    String selectedText = PluginUtils.getSelectedText(editor);

    ToolWindow toolWindow = PluginUtils.getToolWindow(event, Constants.TOOL_WINDOW_ID);
    if (toolWindow == null) {
      GherkinLintLogger.warn("ToolWindow not found for GherkinLint action.");
      return;
    }
    toolWindow.setAutoHide(false);

    ConsoleView consoleView = PluginUtils.createConsoleView(event);
    if (consoleView == null) {
      GherkinLintLogger.warn("Failed to create ConsoleView.");
      return;
    }

    Content content =
        PluginUtils.createToolWindowContent(
            toolWindow, consoleView, Constants.CONTENT_DISPLAY_NAME);
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

    fileType = psiFile.getFileType().getDefaultExtension();
    if (!Constants.SUPPORTED_EXTENSIONS.contains(fileType)) {
      GherkinLintLogger.debug(
          "Validation is not applicable: Unsupported file extension: " + fileType);
      return false;
    }

    String selectedText = caret.getSelectedText();
    String firstLine = selectedText.trim().split("\\R")[0].trim();
    String firstWord = PluginUtils.getFirstKeywordToken(firstLine);

    if (!Constants.GHERKIN_KEYWORDS.contains(firstWord)) {
      GherkinLintLogger.debug(
          "Validation is not applicable: First word is not a Gherkin keyword: " + firstWord);
      return false;
    }

    return true;
  }

  private void validateGherkinText(
      String selectedText, ConsoleView consoleView, @Nullable Project project) {
    if (isEmptyOrInvalidText(selectedText, consoleView)
        || isTooShort(selectedText, consoleView)
        || startsWithNoContextKeyword(selectedText, consoleView)) {
      return;
    }
    if (isThrottled()) {
      showUserMessage(consoleView, Constants.VALIDATION_THROTTLED_ERROR);
      notifyWarning(project, "Validation throttled", Constants.VALIDATION_THROTTLED_ERROR);
      return;
    }

    runValidationTask(project, consoleView, selectedText, fileType);
  }

  private void runValidationTask(
      @Nullable Project project,
      ConsoleView consoleView,
      String selectedText,
      @Nullable String selectedFileType) {
    ProgressManager.getInstance()
        .run(
            new Task.Backgroundable(project, "Validating gherkin text") {
              @Override
              public void run(@NotNull ProgressIndicator indicator) {
                String response;
                try {
                  indicator.setText("Validating Gherkin...");

                  GherkinLintSettingsManager settingsManager = new GherkinLintSettingsManager();
                  ConfigurationManager configurationManager = new ConfigurationManager();
                  JsonNode jsonNode = configurationManager.getFinalConfiguration();
                  PromptBuilder promptBuilder = new PromptBuilder(jsonNode, selectedFileType);
                  String prompt =
                      promptBuilder.buildPrompt(
                          selectedText, !settingsManager.isCustomLogicEnabled());
                  String model = settingsManager.getGeminiModel();

                  GeminiService service = getGeminiService();
                  if (service == null) {
                    showUserMessage(consoleView, Constants.GEMINI_SERVICE_ACCESS_ERROR);
                    return;
                  }

                  response = service.getCompletion(prompt, model);
                  if (response != null) {
                    GherkinLintLogger.debugVerbose(
                        "Received Gemini response (" + response.length() + " chars).");
                  }
                  if (StringUtils.isBlank(response)) {
                    showUserMessage(consoleView, Constants.NO_GEMINI_SERVICE_RESPONSE_ERROR);
                    return;
                  }

                  String finalResponse = GherkinOutputParser.parseOutput(response);
                  if (StringUtils.isBlank(finalResponse)) {
                    showUserMessage(consoleView, Constants.UNKNOWN_ERROR);
                    return;
                  }

                  handleValidResponseAsync(indicator, finalResponse, consoleView);
                } catch (Exception ex) {
                  handleException(consoleView, project, ex);
                }
              }
            });
  }

  private void handleValidResponseAsync(
      @NotNull ProgressIndicator indicator, String finalResponse, ConsoleView consoleView) {
    SwingUtilities.invokeLater(
        () -> {
          GherkinLintLogger.debug("Attempting to print final response to console view.");
          try {
            printStyledOutput(finalResponse, consoleView);
            indicator.setText("Validation complete");
            GherkinLintLogger.debug("Gherkin text validated and ToolWindow content activated.");
          } catch (Exception ex) {
            reportError(
                consoleView,
                Constants.CONSOLE_OUTPUT_PRINT_FAILURE,
                "Consider investigating EDT issues.");
          }
        });
  }

  private static void printStyledOutput(String output, ConsoleView consoleView) {
    String[] lines = output.split("\\n");
    int minPadding = Constants.PROPERTY_SUGGESTION.length();

    for (String line : lines) {
      String[] parts = line.split("\\|", 2);
      String property = parts[0];
      String value = (parts.length > 1) ? parts[1] : "";

      switch (property) {
        case Constants.PROPERTY_TITLE:
          consoleView.print(value + "\n", ConsoleViewContentType.LOG_VERBOSE_OUTPUT);
          break;
        case Constants.PROPERTY_STATUS:
        case Constants.PROPERTY_REASON:
        case Constants.PROPERTY_SUGGESTION:
          consoleView.print(
              String.format("- %-" + minPadding + "s: ", property),
              ConsoleViewContentType.LOG_DEBUG_OUTPUT);
          ConsoleViewContentType contentType =
              (Constants.STATUS_VALID).equals(value)
                  ? ConsoleViewContentType.USER_INPUT
                  : (Constants.STATUS_INVALID).equals(value)
                      ? ConsoleViewContentType.ERROR_OUTPUT
                      : ConsoleViewContentType.NORMAL_OUTPUT;
          consoleView.print(value + "\n", contentType);
          break;
        default:
          int size = line.length() + (Constants.PROPERTY_SUGGESTION.length() + 4);
          consoleView.print(
              StringUtils.leftPad(line, size) + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
      }
    }
  }

  private void reportError(
      ConsoleView consoleView, String errorMessage, @Nullable String additionalInfo) {
    GherkinLintLogger.error(errorMessage);
    consoleView.print(errorMessage, ConsoleViewContentType.ERROR_OUTPUT);
  }

  private void showUserMessage(ConsoleView consoleView, String message) {
    GherkinLintLogger.debug(message);
    consoleView.print(message, ConsoleViewContentType.ERROR_OUTPUT);
  }

  private void notifyError(@Nullable Project project, String title, String message) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("GherkinLint")
        .createNotification(title, message, NotificationType.ERROR)
        .notify(project);
  }

  private void notifyWarning(@Nullable Project project, String title, String message) {
    NotificationGroupManager.getInstance()
        .getNotificationGroup("GherkinLint")
        .createNotification(title, message, NotificationType.WARNING)
        .notify(project);
  }

  private boolean isRateLimitError(@Nullable String message) {
    if (message == null) {
      return false;
    }
    return message.contains("429")
        || message.contains("RESOURCE_EXHAUSTED")
        || message.toLowerCase().contains("rate limit");
  }

  private boolean isModelNotFoundError(@Nullable String message) {
    if (message == null) {
      return false;
    }
    String lower = message.toLowerCase();
    return (lower.contains("404") && lower.contains("not found"))
        || lower.contains("not found for api version")
        || lower.contains("call listmodels");
  }

  private boolean isEmptyOrInvalidText(String text, ConsoleView consoleView) {
    if (text == null || text.trim().isEmpty()) {
      showUserMessage(consoleView, Constants.NO_GHERKIN_TEXT_SELECTED_ERROR);
      return true;
    }
    return false;
  }

  private boolean isTooShort(String text, ConsoleView consoleView) {
    if (text.trim().split("\\s+").length < 4) {
      showUserMessage(consoleView, Constants.GHERKIN_TEXT_TOO_SHORT_ERROR);
      return true;
    }
    return false;
  }

  private boolean startsWithNoContextKeyword(String text, ConsoleView consoleView) {
    if (StringUtils.equalsAnyIgnoreCase(
            Constants.AND_KEYWORD, PluginUtils.getFirstKeywordToken(text))
        || StringUtils.equalsAnyIgnoreCase(
            Constants.BUT_KEYWORD, PluginUtils.getFirstKeywordToken(text))
        || StringUtils.startsWith(text, Constants.ASTERISK_KEYWORD)) {
      showUserMessage(consoleView, Constants.GHERKIN_NO_CONTEXT_ERROR);
      return true;
    }
    return false;
  }

  private void handleException(ConsoleView consoleView, @Nullable Project project, Exception ex) {
    String message = ex.getMessage();
    if (ex instanceof GeminiApiException apiException) {
      handleGeminiApiException(consoleView, project, apiException);
      return;
    }
    if (ex instanceof IllegalStateException && message != null) {
      if (message.contains("No Gemini models available")) {
        showUserMessage(consoleView, Constants.NO_GEMINI_MODELS_AVAILABLE);
        notifyWarning(
            project, "Gemini models unavailable", "Load models in settings and select another.");
        return;
      }
      if (message.contains("Gemini API key is missing")) {
        showUserMessage(consoleView, "No Gemini API key found. Update it in settings.");
        notifyWarning(
            project,
            "Gemini API key missing",
            "Set the key in settings or GOOGLE_API_KEY, then try again.");
        return;
      }
    }
    if (isModelNotFoundError(message)) {
      showUserMessage(consoleView, "Selected Gemini model is unavailable.");
      notifyWarning(
          project, "Gemini model unavailable", "Load models in settings and select another.");
      return;
    }
    if (ex instanceof IllegalArgumentException
        && message != null
        && (message.contains("JSON") || message.contains("Gemini response"))) {
      showUserMessage(consoleView, Constants.UNKNOWN_ERROR);
      notifyWarning(project, "Invalid Gemini response", "Gemini returned an invalid response.");
      return;
    }
    if (isRateLimitError(message)) {
      showUserMessage(consoleView, Constants.GEMINI_RATE_LIMIT_ERROR);
      notifyWarning(
          project,
          "Gemini rate limit exceeded",
          "Request quota exceeded. Try again later or check your Gemini usage limits.");
      return;
    }
    reportError(consoleView, Constants.UNKNOWN_ERROR, ex.getMessage());
  }

  private void handleGeminiApiException(
      ConsoleView consoleView, @Nullable Project project, GeminiApiException ex) {
    String reason = ex.getShortReason();
    String message = ex.getMessage() != null ? ex.getMessage() : "";
    int statusCode = ex.getStatusCode();
    if (isInvalidApiKeyError(ex.getStatusCode(), reason, message)) {
      showUserMessage(consoleView, "Invalid API key. Please update it in settings.");
      notifyWarning(project, "Invalid API key", "Update your Gemini API key in settings.");
      GherkinLintLogger.info("Gemini request failed: " + statusCode + " " + reason);
      return;
    }
    if (statusCode == 404 || isModelNotFoundError(message)) {
      showUserMessage(consoleView, "Selected Gemini model is unavailable.");
      notifyWarning(
          project, "Gemini model unavailable", "Load models in settings and select another.");
      GherkinLintLogger.info("Gemini request failed: " + statusCode + " " + reason);
      return;
    }
    if (statusCode == 429 || isRateLimitError(message)) {
      showUserMessage(consoleView, Constants.GEMINI_RATE_LIMIT_ERROR);
      notifyWarning(
          project,
          "Gemini rate limit exceeded",
          "Request quota exceeded. Try again later or check your Gemini usage limits.");
      GherkinLintLogger.info("Gemini request failed: " + statusCode + " " + reason);
      return;
    }
    if (statusCode >= 500) {
      GherkinLintLogger.warn("Gemini request failed: " + statusCode + " " + reason);
    } else {
      GherkinLintLogger.info("Gemini request failed: " + statusCode + " " + reason);
    }
    showUserMessage(consoleView, "Gemini request failed. Please try again.");
  }

  private boolean isInvalidApiKeyError(int statusCode, String reason, String message) {
    String combined = (reason + " " + message).toLowerCase();
    return statusCode == 400
        && (combined.contains("api_key_invalid")
            || combined.contains("invalid_argument")
            || combined.contains("invalid api key"));
  }

  private GeminiService getGeminiService() {
    GherkinLintServiceImpl gherkinLintServiceImpl =
        ApplicationManager.getApplication().getService(GherkinLintServiceImpl.class);
    return gherkinLintServiceImpl.getGeminiService();
  }

  private static boolean isThrottled() {
    long now = System.currentTimeMillis();
    if (now - lastValidationAt < Constants.VALIDATION_THROTTLE_MS) {
      return true;
    }
    lastValidationAt = now;
    return false;
  }
}
