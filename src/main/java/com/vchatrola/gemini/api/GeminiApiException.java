package com.vchatrola.gemini.api;

public class GeminiApiException extends RuntimeException {
  private final int statusCode;
  private final String shortReason;

  public GeminiApiException(int statusCode, String shortReason, String message) {
    super(message);
    this.statusCode = statusCode;
    this.shortReason = shortReason;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getShortReason() {
    return shortReason;
  }
}
