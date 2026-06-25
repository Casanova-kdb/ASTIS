package com.astis.recommendation.controller;

import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
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

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class RecommendationControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        behaviorLogRepository.deleteAll();
        taskRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void recommendedTasksRequireAuthentication() throws Exception {
        mockMvc.perform(get("/recommendations/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanViewRankedRecommendedTasks() throws Exception {
        String token = registerAndToken("student", "student@example.com");
        AppUser user = appUserRepository.findByEmail("student@example.com").orElseThrow();
        AppUser otherUser = appUserRepository.save(new AppUser("other", "other@example.com", "password-hash"));

        Task highPriorityTask = taskRepository.save(new Task(
                user,
                "Finish AI Coursework",
                "Complete recommendation module",
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(2),
                BigDecimal.valueOf(6.0)
        ));

        taskRepository.save(new Task(
                user,
                "Read Research Paper",
                "Read one paper",
                "READING",
                TaskPriority.LOW,
                LocalDateTime.now().plusDays(14),
                BigDecimal.valueOf(1.0)
        ));

        Task completedTask = new Task(
                user,
                "Completed Lab",
                null,
                "LAB",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(1),
                BigDecimal.valueOf(3.0)
        );
        completedTask.updateStatus(TaskStatus.COMPLETED);
        taskRepository.save(completedTask);

        taskRepository.save(new Task(
                otherUser,
                "Other User Coursework",
                null,
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(1),
                BigDecimal.valueOf(8.0)
        ));

        mockMvc.perform(get("/recommendations/tasks")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].rankPosition").value(1))
                .andExpect(jsonPath("$.data[0].taskId").value(highPriorityTask.getId()))
                .andExpect(jsonPath("$.data[0].title").value("Finish AI Coursework"))
                .andExpect(jsonPath("$.data[0].priorityScore", greaterThan(0.0)))
                .andExpect(jsonPath("$.data[0].delayRisk").value("LOW"))
                .andExpect(jsonPath("$.data[0].reason", not(blankOrNullString())))
                .andExpect(jsonPath("$.data[1].rankPosition").value(2))
                .andExpect(jsonPath("$.data[1].title").value("Read Research Paper"));
    }

    @Test
    void emptyRecommendationListReturnsEmptyData() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        mockMvc.perform(get("/recommendations/tasks")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(0));
    }

    private String registerAndToken(String username, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "%s",
                                  "email": "%s",
                                  "password": "password123"
                                }
                                """.formatted(username, email)))
                .andExpect(status().isCreated())
                .andReturn();

        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
