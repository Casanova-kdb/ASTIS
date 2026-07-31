package com.astis.config;

import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;

class RecommendationCacheSerializationTests {

    @Test
    void recommendationListCanBeWrittenToAndReadFromRedisJson() {
        Jackson2JsonRedisSerializer<List<RecommendedTaskResponse>> serializer =
                CacheConfig.recommendationValueSerializer();
        RecommendedTaskResponse recommendation = new RecommendedTaskResponse(
                1,
                42L,
                "Finish coursework",
                "COURSEWORK",
                TaskPriority.HIGH,
                TaskStatus.PENDING,
                LocalDateTime.of(2027, 6, 24, 18, 0),
                BigDecimal.valueOf(4.5),
                87.25,
                "HIGH",
                "Deadline is coming soon.",
                List.of("the deadline is coming soon"),
                "High delay risk is driven by the current workload."
        );

        byte[] cachedValue = serializer.serialize(List.of(recommendation));
        List<RecommendedTaskResponse> restoredValue = serializer.deserialize(cachedValue);

        assertThat(restoredValue)
                .singleElement()
                .isEqualTo(recommendation);
    }
}
