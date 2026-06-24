package com.astis.user.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(
        String secret,
        long expirationMs
) {
    private static final String DEFAULT_SECRET = "astis-development-secret-key-change-before-production-2026";
    private static final long DEFAULT_EXPIRATION_MS = 86400000L;

    public JwtProperties {
        if (secret == null || secret.isBlank()) {
            secret = DEFAULT_SECRET;
        }
        if (expirationMs <= 0) {
            expirationMs = DEFAULT_EXPIRATION_MS;
        }
    }
}
