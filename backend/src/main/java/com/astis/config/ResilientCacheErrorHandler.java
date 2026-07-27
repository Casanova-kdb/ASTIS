package com.astis.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class ResilientCacheErrorHandler implements CacheErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResilientCacheErrorHandler.class);

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        logFailure("read", exception, cache, key);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        logFailure("write", exception, cache, key);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        logFailure("evict", exception, cache, key);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        logFailure("clear", exception, cache, null);
    }

    private void logFailure(String operation, RuntimeException exception, Cache cache, Object key) {
        LOGGER.warn(
                "Redis cache {} failed for cache={} key={}; continuing without cached data. reason={}",
                operation,
                cache.getName(),
                key,
                exception.getMessage()
        );
    }
}
