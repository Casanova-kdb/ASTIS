package com.astis.config;

import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    @Bean
    @ConditionalOnProperty(
            prefix = "app.cache.redis",
            name = "enabled",
            havingValue = "true",
            matchIfMissing = true
    )
    public CacheManager redisCacheManager(
            RedisConnectionFactory connectionFactory,
            RedisCacheProperties properties
    ) {
        RedisCacheConfiguration recommendationConfiguration = RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(properties.recommendationTtl())
                .disableCachingNullValues()
                .prefixCacheNameWith("astis::")
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new StringRedisSerializer()
                ))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        recommendationValueSerializer()
                ));

        return RedisCacheManager.builder(connectionFactory)
                .withInitialCacheConfigurations(Map.of(
                        CacheNames.USER_RECOMMENDATIONS,
                        recommendationConfiguration
                ))
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager localCacheManager() {
        return new ConcurrentMapCacheManager(CacheNames.USER_RECOMMENDATIONS);
    }

    @Override
    public CacheErrorHandler errorHandler() {
        return new ResilientCacheErrorHandler();
    }

    static Jackson2JsonRedisSerializer<List<RecommendedTaskResponse>> recommendationValueSerializer() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        JavaType recommendationListType = objectMapper
                .getTypeFactory()
                .constructCollectionType(ArrayList.class, RecommendedTaskResponse.class);

        return new Jackson2JsonRedisSerializer<>(objectMapper, recommendationListType);
    }
}
