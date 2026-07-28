package com.astis.studyplan.controller;

import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.config.CacheNames;
import com.astis.settings.entity.DailyStudyCapacity;
import com.astis.settings.entity.DeadlinePressureTolerance;
import com.astis.settings.entity.PlanningStyle;
import com.astis.settings.entity.PreferredStudyTime;
import com.astis.settings.entity.StudyPace;
import com.astis.settings.entity.UserProfile;
import com.astis.settings.repository.UserProfileRepository;
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
import org.springframework.cache.CacheManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudyPlanControllerIntegrationTests {

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

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCache(CacheNames.USER_RECOMMENDATIONS).clear();
        behaviorLogRepository.deleteAll();
        taskRepository.deleteAll();
        userProfileRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void studyPlanRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/study-plans"))
                .andExpect(status().isForbidden());
    }

    @Test
    void emptyTaskListReturnsAnEmptyPlan() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        mockMvc.perform(get("/study-plans")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planningDays").value(7))
                .andExpect(jsonPath("$.data.totalScheduledHours").value(0.0))
                .andExpect(jsonPath("$.data.totalUnscheduledHours").value(0.0))
                .andExpect(jsonPath("$.data.overloaded").value(false))
                .andExpect(jsonPath("$.data.warnings").isEmpty())
                .andExpect(jsonPath("$.data.unscheduledTasks").isEmpty())
                .andExpect(jsonPath("$.data.days.length()").value(7));
    }

    @Test
    void userCanGenerateDefaultSevenDayPlanFromOwnActiveTasks() throws Exception {
        String token = registerAndToken("student", "student@example.com");
        AppUser user = appUserRepository.findByEmail("student@example.com").orElseThrow();
        AppUser otherUser = appUserRepository.save(new AppUser(
                "other",
                "other@example.com",
                "password-hash"
        ));

        taskRepository.save(new Task(
                user,
                "Own active task",
                null,
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(10),
                new BigDecimal("2.00")
        ));
        Task completedTask = new Task(
                user,
                "Own completed task",
                null,
                "REVISION",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(2),
                new BigDecimal("4.00")
        );
        completedTask.updateStatus(TaskStatus.COMPLETED);
        taskRepository.save(completedTask);
        taskRepository.save(new Task(
                otherUser,
                "Another user's task",
                null,
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(1),
                new BigDecimal("8.00")
        ));

        mockMvc.perform(get("/study-plans")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Study plan generated"))
                .andExpect(jsonPath("$.data.planningDays").value(7))
                .andExpect(jsonPath("$.data.preferredStudyTime").value("EVENING"))
                .andExpect(jsonPath("$.data.dailyCapacityHours").value(3.0))
                .andExpect(jsonPath("$.data.totalAvailableHours").value(21.0))
                .andExpect(jsonPath("$.data.totalScheduledHours").value(2.0))
                .andExpect(jsonPath("$.data.totalUnscheduledHours").value(0.0))
                .andExpect(jsonPath("$.data.overloaded").value(false))
                .andExpect(jsonPath("$.data.days.length()").value(7))
                .andExpect(jsonPath("$.data.days[*].sessions[*].title", hasItem("Own active task")))
                .andExpect(jsonPath(
                        "$.data.days[*].sessions[*].title",
                        not(hasItem("Another user's task"))
                ));
    }

    @Test
    void generatedPlanUsesTheUsersProfileSettings() throws Exception {
        String token = registerAndToken("student", "student@example.com");
        AppUser user = appUserRepository.findByEmail("student@example.com").orElseThrow();
        UserProfile profile = new UserProfile(user);
        profile.update(
                StudyPace.SLOW,
                DeadlinePressureTolerance.LOW,
                DailyStudyCapacity.HEAVY,
                PreferredStudyTime.NIGHT,
                PlanningStyle.STRICT
        );
        userProfileRepository.save(profile);

        taskRepository.save(new Task(
                user,
                "Prepare major coursework",
                null,
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(30),
                new BigDecimal("8.00")
        ));

        mockMvc.perform(get("/study-plans")
                        .queryParam("days", "3")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.planningDays").value(3))
                .andExpect(jsonPath("$.data.preferredStudyTime").value("NIGHT"))
                .andExpect(jsonPath("$.data.dailyCapacityHours").value(5.0))
                .andExpect(jsonPath("$.data.totalAvailableHours").value(15.0))
                .andExpect(jsonPath("$.data.totalScheduledHours").value(8.0));
    }

    @Test
    void workloadThatCannotFitBeforeDeadlineReturnsAnOverloadWarning() throws Exception {
        String token = registerAndToken("student", "student@example.com");
        AppUser user = appUserRepository.findByEmail("student@example.com").orElseThrow();

        taskRepository.save(new Task(
                user,
                "Large urgent coursework",
                null,
                "COURSEWORK",
                TaskPriority.HIGH,
                LocalDateTime.now().plusDays(1).withHour(23).withMinute(0),
                new BigDecimal("10.00")
        ));

        mockMvc.perform(get("/study-plans")
                        .queryParam("days", "2")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.overloaded").value(true))
                .andExpect(jsonPath(
                        "$.data.warnings[*].code",
                        hasItem("INSUFFICIENT_CAPACITY")
                ))
                .andExpect(jsonPath(
                        "$.data.unscheduledTasks[*].title",
                        hasItem("Large urgent coursework")
                ))
                .andExpect(jsonPath(
                        "$.data.unscheduledTasks[*].reason",
                        hasItem("INSUFFICIENT_CAPACITY")
                ));
    }

    @Test
    void invalidPlanningWindowReturnsBadRequest() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        mockMvc.perform(get("/study-plans")
                        .queryParam("days", "0")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Planning days must be between 1 and 14"));

        mockMvc.perform(get("/study-plans")
                        .queryParam("days", "15")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Planning days must be between 1 and 14"));

        mockMvc.perform(get("/study-plans")
                        .queryParam("days", "abc")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("days: must be a valid value"));
    }

    @Test
    void taskCreationInvalidatesRecommendationsUsedByTheNextPlan() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        mockMvc.perform(get("/study-plans")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalScheduledHours").value(0.0));

        mockMvc.perform(post("/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "New planning task",
                                  "description": "Verify regeneration after a task change",
                                  "taskType": "COURSEWORK",
                                  "priority": "HIGH",
                                  "deadline": "2099-08-15T18:00:00",
                                  "estimatedHours": 2.0
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/study-plans")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalScheduledHours").value(2.0))
                .andExpect(jsonPath("$.data.days[*].sessions[*].title", hasItem("New planning task")));
    }

    @Test
    void studyPlanEndpointAppearsInOpenApiDocumentation() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$['components']['securitySchemes']['bearerAuth']['scheme']")
                        .value("bearer"))
                .andExpect(jsonPath("$['paths']['/study-plans']['get']['summary']")
                        .value("Generate a study plan"))
                .andExpect(jsonPath("$['paths']['/study-plans']['get']['security'][0]['bearerAuth']")
                        .isArray());
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

        return com.jayway.jsonpath.JsonPath.read(
                result.getResponse().getContentAsString(),
                "$.data.accessToken"
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
