package com.vchatrola.plugin.service;

import com.intellij.openapi.components.Service;
import com.vchatrola.util.GherkinLintLogger;
import com.vchatrola.gemini.config.AppConfig;
import com.vchatrola.gemini.service.GeminiService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Service
public final class GherkinLintServiceImpl implements GherkinLintService {

    private final GeminiService geminiService;

    public GherkinLintServiceImpl() {
        // Use a custom class loader if necessary
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        AnnotationConfigApplicationContext applicationContext = null;

        try {
            Thread.currentThread().setContextClassLoader(GherkinLintServiceImpl.class.getClassLoader());

            // Initialize Spring context
            applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
            applicationContext.register(GeminiService.class);

            // Get the Spring service instance
            this.geminiService = applicationContext.getBean(GeminiService.class);
            GherkinLintLogger.info("GeminiService has been successfully initialized.");
        } catch (Exception e) {
            GherkinLintLogger.error("Failed to initialize GeminiService.", e);
            throw new RuntimeException("Failed to initialize GeminiService.", e);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
            if (applicationContext != null && applicationContext.isActive()) {
                applicationContext.close();
                GherkinLintLogger.info("Spring application context has been closed.");
            }
        }
    }

    @Override
    public GeminiService getGeminiService() {
        return geminiService;
    }
}