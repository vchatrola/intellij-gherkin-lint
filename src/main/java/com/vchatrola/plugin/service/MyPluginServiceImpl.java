package com.vchatrola.plugin.service;

import com.intellij.openapi.components.Service;
import com.vchatrola.gemini.config.AppConfig;
import com.vchatrola.gemini.service.GeminiService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@Service
public final class MyPluginServiceImpl implements MyPluginService {

    private final GeminiService geminiService;

    public MyPluginServiceImpl() {
        // Use a custom class loader if necessary
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(MyPluginServiceImpl.class.getClassLoader());

            // Initialize Spring context
            AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext(AppConfig.class);
            applicationContext.register(GeminiService.class);

            // Get the Spring service instance
            this.geminiService = applicationContext.getBean(GeminiService.class);
        } finally {
            Thread.currentThread().setContextClassLoader(originalClassLoader);
        }
    }

    @Override
    public GeminiService getGeminiService() {
        return geminiService;
    }
}