package com.astis.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cache.redis")
public record RedisCacheProperties(
        boolean enabled,
        Duration recommendationTtl
) {
}
