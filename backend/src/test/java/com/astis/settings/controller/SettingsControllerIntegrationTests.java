package com.astis.settings.controller;

import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.settings.repository.UserProfileRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SettingsControllerIntegrationTests {

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
    void profileSettingsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/settings/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    void userCanViewAndUpdateProfileSettings() throws Exception {
        String token = registerAndToken("student", "student@example.com");

        mockMvc.perform(get("/settings/profile")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studyPace").value("NORMAL"))
                .andExpect(jsonPath("$.data.deadlinePressureTolerance").value("MEDIUM"))
                .andExpect(jsonPath("$.data.dailyStudyCapacity").value("MEDIUM"))
                .andExpect(jsonPath("$.data.preferredStudyTime").value("EVENING"))
                .andExpect(jsonPath("$.data.planningStyle").value("BALANCED"));

        mockMvc.perform(put("/settings/profile")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "studyPace": "SLOW",
                                  "deadlinePressureTolerance": "LOW",
                                  "dailyStudyCapacity": "LIGHT",
                                  "preferredStudyTime": "MORNING",
                                  "planningStyle": "STRICT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.studyPace").value("SLOW"))
                .andExpect(jsonPath("$.data.deadlinePressureTolerance").value("LOW"))
                .andExpect(jsonPath("$.data.dailyStudyCapacity").value("LIGHT"))
                .andExpect(jsonPath("$.data.preferredStudyTime").value("MORNING"))
                .andExpect(jsonPath("$.data.planningStyle").value("STRICT"));
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
