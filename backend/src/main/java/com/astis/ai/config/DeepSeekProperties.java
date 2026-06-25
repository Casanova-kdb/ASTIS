package com.astis.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.ai.deepseek")
public record DeepSeekProperties(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String model,
        double temperature,
        int maxTokens
) {
    public boolean isConfigured() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }
}
