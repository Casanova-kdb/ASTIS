package com.astis.config;

import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.settings.repository.UserProfileRepository;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.cache.redis.enabled=true",
        "spring.data.redis.host=127.0.0.1",
        "spring.data.redis.port=1",
        "spring.data.redis.connect-timeout=100ms",
        "spring.data.redis.timeout=100ms"
})
@AutoConfigureMockMvc
class RedisUnavailableFallbackIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        behaviorLogRepository.deleteAll();
        taskRepository.deleteAll();
        userProfileRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void recommendationsStillUseDatabaseWhenRedisIsUnavailable() throws Exception {
        String token = registerAndToken();
        AppUser user = appUserRepository.findByEmail("fallback@example.com").orElseThrow();
        taskRepository.save(new Task(
                user,
                "Redis fallback task",
                "Recommendation should still be calculated",
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(2),
                BigDecimal.valueOf(3.0)
        ));

        mockMvc.perform(get("/recommendations/tasks")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Redis fallback task"));
    }

    private String registerAndToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "fallback-student",
                                  "email": "fallback@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.accessToken"
        );
    }
}
