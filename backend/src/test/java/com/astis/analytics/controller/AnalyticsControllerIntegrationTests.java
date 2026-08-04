package com.astis.analytics.controller;

import com.astis.analytics.entity.BehaviorActionType;
import com.astis.analytics.entity.BehaviorLog;
import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.settings.repository.UserProfileRepository;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskPriority;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.hasSize;
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

    @Autowired
    private UserProfileRepository userProfileRepository;

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
    void analyticsTrendsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/analytics/trends"))
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

    @Test
    void defaultTrendRequestReturnsEightZeroFilledWeeks() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        MvcResult result = mockMvc.perform(get("/analytics/trends")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Analytics trends retrieved"))
                .andExpect(jsonPath("$.data.weeks").value(8))
                .andExpect(jsonPath("$.data.weeklyTrends", hasSize(8)))
                .andExpect(jsonPath("$.data.mostDelayedTaskType").doesNotExist())
                .andExpect(jsonPath("$.data.averageEstimatedHours").value(0.0))
                .andReturn();

        List<Integer> completionCounts = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.weeklyTrends..completedCount"
        );
        List<Integer> overdueCounts = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.weeklyTrends..overdueCount"
        );
        assertThat(completionCounts).containsExactly(0, 0, 0, 0, 0, 0, 0, 0);
        assertThat(overdueCounts).containsExactly(0, 0, 0, 0, 0, 0, 0, 0);
    }

    @Test
    void trendRequestAcceptsFourAndTwelveWeekWindows() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        mockMvc.perform(get("/analytics/trends")
                        .param("weeks", "4")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weeks").value(4))
                .andExpect(jsonPath("$.data.weeklyTrends", hasSize(4)));

        mockMvc.perform(get("/analytics/trends")
                        .param("weeks", "12")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.weeks").value(12))
                .andExpect(jsonPath("$.data.weeklyTrends", hasSize(12)));
    }

    @Test
    void trendRequestRejectsInvalidWeekValues() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        mockMvc.perform(get("/analytics/trends")
                        .param("weeks", "3")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Analytics weeks must be between 4 and 12"));

        mockMvc.perform(get("/analytics/trends")
                        .param("weeks", "13")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Analytics weeks must be between 4 and 12"));

        mockMvc.perform(get("/analytics/trends")
                        .param("weeks", "eight")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("weeks: must be a valid value"));
    }

    @Test
    void trendRequestOnlyUsesAuthenticatedUsersData() throws Exception {
        String token = registerAndToken("student", "student@example.com");
        AppUser user = appUserRepository.findByEmail("student@example.com").orElseThrow();
        AppUser otherUser = appUserRepository.save(new AppUser("other", "other@example.com", "password-hash"));

        taskRepository.save(new Task(
                user,
                "Late coursework",
                null,
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().minusHours(2),
                new BigDecimal("2.00")
        ));
        taskRepository.save(new Task(
                user,
                "Upcoming exam",
                null,
                "EXAM",
                TaskPriority.MEDIUM,
                LocalDateTime.now().plusDays(2),
                new BigDecimal("4.00")
        ));
        taskRepository.save(new Task(
                otherUser,
                "Other user's late task",
                null,
                "READING",
                TaskPriority.HIGH,
                LocalDateTime.now().minusHours(2),
                new BigDecimal("20.00")
        ));
        behaviorLogRepository.save(new BehaviorLog(
                user.getId(),
                null,
                BehaviorActionType.COMPLETE_TASK,
                "IN_PROGRESS",
                "COMPLETED"
        ));
        behaviorLogRepository.save(new BehaviorLog(
                otherUser.getId(),
                null,
                BehaviorActionType.COMPLETE_TASK,
                "IN_PROGRESS",
                "COMPLETED"
        ));

        MvcResult result = mockMvc.perform(get("/analytics/trends")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.mostDelayedTaskType.taskType").value("COURSEWORK"))
                .andExpect(jsonPath("$.data.mostDelayedTaskType.delayedCount").value(1))
                .andExpect(jsonPath("$.data.averageEstimatedHours").value(3.0))
                .andReturn();

        List<Integer> completionCounts = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.weeklyTrends..completedCount"
        );
        List<Integer> overdueCounts = com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.weeklyTrends..overdueCount"
        );
        assertThat(completionCounts.stream().mapToInt(Integer::intValue).sum()).isEqualTo(1);
        assertThat(overdueCounts.stream().mapToInt(Integer::intValue).sum()).isEqualTo(1);
    }

    @Test
    void trendsEndpointAppearsInOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/analytics/trends']").exists());
    }

    private void cleanDatabase() {
        behaviorLogRepository.deleteAll();
        taskRepository.deleteAll();
        userProfileRepository.deleteAll();
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
