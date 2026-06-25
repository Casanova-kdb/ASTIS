package com.astis.analytics.controller;

import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.closeTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AnalyticsControllerIntegrationTests {

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
        cleanDatabase();
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    @Test
    void analyticsSummaryRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/analytics/summary"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanViewOwnAnalyticsSummary() throws Exception {
        String token = registerAndToken("student", "student@example.com");
        AppUser user = appUserRepository.findByEmail("student@example.com").orElseThrow();
        AppUser otherUser = appUserRepository.save(new AppUser("other", "other@example.com", "password-hash"));

        Task completedTask = new Task(
                user,
                "Completed Essay",
                "Submitted on time",
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(1),
                null
        );
        completedTask.updateStatus(TaskStatus.COMPLETED);

        Task overdueTask = new Task(
                user,
                "Overdue Reading",
                "Read chapter 3",
                "READING",
                TaskPriority.MEDIUM,
                LocalDateTime.now().minusDays(1),
                null
        );

        Task otherUsersTask = new Task(
                otherUser,
                "Other User Task",
                null,
                "COURSEWORK",
                TaskPriority.LOW,
                LocalDateTime.now().minusDays(1),
                null
        );

        taskRepository.save(completedTask);
        taskRepository.save(overdueTask);
        taskRepository.save(otherUsersTask);

        mockMvc.perform(get("/analytics/summary")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalTaskCount").value(2))
                .andExpect(jsonPath("$.data.completedTaskCount").value(1))
                .andExpect(jsonPath("$.data.overdueTaskCount").value(1))
                .andExpect(jsonPath("$.data.completionRate").value(closeTo(0.5, 0.001)));
    }

    @Test
    void emptyAnalyticsSummaryReturnsZeroValues() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        mockMvc.perform(get("/analytics/summary")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalTaskCount").value(0))
                .andExpect(jsonPath("$.data.completedTaskCount").value(0))
                .andExpect(jsonPath("$.data.overdueTaskCount").value(0))
                .andExpect(jsonPath("$.data.completionRate").value(0.0));
    }

    private void cleanDatabase() {
        behaviorLogRepository.deleteAll();
        taskRepository.deleteAll();
        appUserRepository.deleteAll();
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
