package com.vchatrola.plugin.setting;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;

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
      return stored;
    }
    String env = System.getenv("GOOGLE_API_KEY");
    return env == null ? "" : env;
  }

  public static boolean hasApiKey() {
    return !loadApiKey().isBlank();
  }
}
