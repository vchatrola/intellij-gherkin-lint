package com.vchatrola.util;

public class GherkinLintLogger {
  private static final java.util.logging.Logger LOGGER =
      java.util.logging.Logger.getLogger("GherkinLintLogger");
  private static volatile boolean verboseEnabled = false;

  private GherkinLintLogger() {
    // Private constructor to prevent instantiation
  }

  public static void debug(String message) {
    LOGGER.fine(message);
  }

  public static void debug(String message, Throwable t) {
    LOGGER.log(java.util.logging.Level.FINE, message, t);
  }

  public static void debugVerbose(String message) {
    if (verboseEnabled) {
      LOGGER.info(message);
    }
  }

  public static void info(String message) {
    LOGGER.info(message);
  }

  public static void info(String message, Throwable t) {
    LOGGER.log(java.util.logging.Level.INFO, message, t);
  }

  public static void warn(String message) {
    LOGGER.warning(message);
  }

  public static void warn(String message, Throwable t) {
    LOGGER.log(java.util.logging.Level.WARNING, message, t);
  }

  public static void error(String message) {
    LOGGER.severe(message);
  }

  public static void error(String message, Throwable t) {
    LOGGER.log(java.util.logging.Level.SEVERE, message, t);
  }

  public static void setVerboseEnabled(boolean enabled) {
    verboseEnabled = enabled;
  }
}
