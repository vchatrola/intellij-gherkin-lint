package com.vchatrola.util;


import com.intellij.openapi.diagnostic.Logger;

public class GherkinLintLogger {
    private static final Logger LOGGER = Logger.getInstance("GherkinLintLogger");

    private GherkinLintLogger() {
        // Private constructor to prevent instantiation
    }

    public static void debug(String message) {
        LOGGER.debug(message);
    }

    public static void debug(String message, Throwable t) {
        LOGGER.debug(message, t);
    }

    public static void info(String message) {
        LOGGER.info(message);
    }

    public static void info(String message, Throwable t) {
        LOGGER.info(message, t);
    }

    public static void warn(String message) {
        LOGGER.warn(message);
    }

    public static void warn(String message, Throwable t) {
        LOGGER.warn(message, t);
    }

    public static void error(String message) {
        LOGGER.error(message);
    }

    public static void error(String message, Throwable t) {
        LOGGER.error(message, t);
    }
}
