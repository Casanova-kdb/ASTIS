package com.astis.task.controller;

import com.astis.analytics.entity.BehaviorActionType;
import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.task.repository.TaskRepository;
import com.astis.user.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        behaviorLogRepository.deleteAll();
        taskRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void createTaskRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTaskJson("Database Coursework")))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanCreateAndReadOwnTask() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        Integer taskId = createTask(token, "Database Coursework");

        mockMvc.perform(get("/tasks/{taskId}", taskId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Database Coursework"))
                .andExpect(jsonPath("$.data.priority").value("HIGH"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));

        assertThat(behaviorLogRepository.countByActionType(BehaviorActionType.CREATE_TASK))
                .isEqualTo(1);
    }

    @Test
    void userCanListTasksByStatus() throws Exception {
        String token = registerAndToken("student", "student@example.com");
        createTask(token, "Database Coursework");

        mockMvc.perform(get("/tasks")
                        .queryParam("status", "PENDING")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].title").value("Database Coursework"));
    }

    @Test
    void userCanUpdateTaskDetailsAndStatus() throws Exception {
        String token = registerAndToken("student", "student@example.com");
        Integer taskId = createTask(token, "Database Coursework");

        mockMvc.perform(put("/tasks/{taskId}", taskId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated Database Coursework",
                                  "description": "Finish schema and SQL queries",
                                  "taskType": "COURSEWORK",
                                  "priority": "MEDIUM",
                                  "deadline": "2099-06-25T23:59:00",
                                  "estimatedHours": 4.5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Database Coursework"))
                .andExpect(jsonPath("$.data.priority").value("MEDIUM"));

        assertThat(behaviorLogRepository.countByActionType(BehaviorActionType.UPDATE_TASK))
                .isEqualTo(1);
        assertThat(behaviorLogRepository.countByActionType(BehaviorActionType.UPDATE_DEADLINE))
                .isEqualTo(1);
        assertThat(behaviorLogRepository.countByActionType(BehaviorActionType.UPDATE_PRIORITY))
                .isEqualTo(1);

        mockMvc.perform(patch("/tasks/{taskId}/status", taskId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "COMPLETED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                .andExpect(jsonPath("$.data.completedAt", not(blankOrNullString())));

        assertThat(behaviorLogRepository.countByActionType(BehaviorActionType.UPDATE_STATUS))
                .isEqualTo(1);
        assertThat(behaviorLogRepository.countByActionType(BehaviorActionType.COMPLETE_TASK))
                .isEqualTo(1);
    }

    @Test
    void userCannotReadAnotherUsersTask() throws Exception {
        String firstToken = registerAndToken("student1", "student1@example.com");
        String secondToken = registerAndToken("student2", "student2@example.com");
        Integer taskId = createTask(firstToken, "Private Coursework");

        mockMvc.perform(get("/tasks/{taskId}", taskId)
                        .header("Authorization", bearer(secondToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void userCanDeleteOwnTask() throws Exception {
        String token = registerAndToken("student", "student@example.com");
        Integer taskId = createTask(token, "Database Coursework");

        mockMvc.perform(delete("/tasks/{taskId}", taskId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(behaviorLogRepository.countByActionType(BehaviorActionType.DELETE_TASK))
                .isEqualTo(1);

        mockMvc.perform(get("/tasks/{taskId}", taskId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isNotFound());
    }

    private Integer createTask(String token, String title) throws Exception {
        MvcResult result = mockMvc.perform(post("/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTaskJson(title)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value(title))
                .andReturn();

        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data.id");
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

    private String createTaskJson(String title) {
        return """
                {
                  "title": "%s",
                  "description": "Finish schema and SQL queries",
                  "taskType": "COURSEWORK",
                  "priority": "HIGH",
                  "deadline": "2099-06-24T23:59:00",
                  "estimatedHours": 6.0
                }
                """.formatted(title);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
