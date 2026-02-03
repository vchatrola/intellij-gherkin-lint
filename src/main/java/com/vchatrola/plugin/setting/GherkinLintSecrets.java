package com.vchatrola.plugin.setting;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.vchatrola.util.GherkinLintLogger;

public final class GherkinLintSecrets {
  private static final String SERVICE_NAME = "GherkinLint:GeminiApiKey";

  private GherkinLintSecrets() {}

  public static void saveApiKey(String apiKey) {
    CredentialAttributes attributes = new CredentialAttributes(SERVICE_NAME);
    Credentials credentials =
        (apiKey == null || apiKey.isBlank()) ? null : new Credentials("gemini", apiKey);
    PasswordSafe.getInstance().set(attributes, credentials);
  }

  public static String loadApiKey() {
    CredentialAttributes attributes = new CredentialAttributes(SERVICE_NAME);
    Credentials credentials = PasswordSafe.getInstance().get(attributes);
    if (credentials == null) {
      return "";
    }
    String password = credentials.getPasswordAsString();
    return password == null ? "" : password;
  }

  public static String getApiKeyOrEnv() {
    String stored = loadApiKey();
    if (!stored.isBlank()) {
      GherkinLintLogger.info("Using Gemini API key from IDE storage (masked): " + maskKey(stored));
      return stored;
    }
    String env = System.getenv("GOOGLE_API_KEY");
    if (env != null && !env.isBlank()) {
      GherkinLintLogger.info("Using Gemini API key from environment (masked): " + maskKey(env));
    }
    return env == null ? "" : env;
  }

  public static boolean hasApiKey() {
    return !loadApiKey().isBlank();
  }

  private static String maskKey(String key) {
    String trimmed = key == null ? "" : key.trim();
    if (trimmed.isEmpty()) {
      return "<empty>";
    }
    int keep = Math.min(4, trimmed.length());
    String suffix = trimmed.substring(trimmed.length() - keep);
    return "****" + suffix + " (len=" + trimmed.length() + ")";
  }
}
