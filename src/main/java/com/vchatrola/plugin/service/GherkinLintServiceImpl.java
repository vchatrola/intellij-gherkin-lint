package com.vchatrola.plugin.service;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.vchatrola.gemini.service.GeminiService;

@Service
public final class GherkinLintServiceImpl implements GherkinLintService, Disposable {

  private final GeminiService geminiService;

  public GherkinLintServiceImpl() {
    this.geminiService = new GeminiService();
  }

  @Override
  public GeminiService getGeminiService() {
    return geminiService;
  }

  @Override
  public void dispose() {}
}
