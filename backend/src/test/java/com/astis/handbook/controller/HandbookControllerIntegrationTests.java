package com.astis.handbook.controller;

import com.astis.analytics.repository.BehaviorLogRepository;
import com.astis.settings.repository.UserProfileRepository;
import com.astis.task.repository.TaskRepository;
import com.astis.user.repository.AppUserRepository;
import java.io.ByteArrayOutputStream;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class HandbookControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BehaviorLogRepository behaviorLogRepository;

    @BeforeEach
    void setUp() {
        behaviorLogRepository.deleteAll();
        taskRepository.deleteAll();
        userProfileRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void parseHandbookRequiresAuthentication() throws Exception {
        mockMvc.perform(multipart("/handbooks/parse").file(docxFile()))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserCanParseDocxWithLocalFallback() throws Exception {
        String token = registerAndToken();

        mockMvc.perform(multipart("/handbooks/parse")
                        .file(docxFile())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.provider").value("local-fallback"))
                .andExpect(jsonPath("$.data.fallback").value(true))
                .andExpect(jsonPath("$.data.fallbackReason").isNotEmpty())
                .andExpect(jsonPath("$.data.drafts", hasSize(1)))
                .andExpect(jsonPath("$.data.drafts[0].deadline").value("2099-06-24T23:59:00"));
    }

    @Test
    void authenticatedUserReceivesClearErrorForUnsupportedFile() throws Exception {
        String token = registerAndToken();
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "handbook.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "coursework".getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/handbooks/parse")
                        .file(file)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only PDF and DOCX handbook files are supported"));
    }

    @Test
    void confirmedDraftCanBeCreatedThroughExistingTaskApi() throws Exception {
        String token = registerAndToken();
        MvcResult parseResult = mockMvc.perform(multipart("/handbooks/parse")
                        .file(docxFile())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = parseResult.getResponse().getContentAsString();
        String title = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.drafts[0].title");
        String taskType = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.drafts[0].taskType");
        String priority = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.drafts[0].priority");
        String deadline = com.jayway.jsonpath.JsonPath.read(responseBody, "$.data.drafts[0].deadline");

        mockMvc.perform(post("/tasks")
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "%s",
                                  "description": "Confirmed from handbook draft",
                                  "taskType": "%s",
                                  "priority": "%s",
                                  "deadline": "%s"
                                }
                                """.formatted(title, taskType, priority, deadline)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value(title))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    private String registerAndToken() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "handbook.student",
                                  "email": "handbook.student@example.com",
                                  "password": "password123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return com.jayway.jsonpath.JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    private MockMultipartFile docxFile() throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText("Web Application Development Coursework");
            document.createParagraph().createRun().setText("This coursework is worth 100% of the module marks.");
            document.createParagraph().createRun().setText("Submission deadline: 2099-06-24.");
            document.write(output);

            return new MockMultipartFile(
                    "file",
                    "handbook.docx",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    output.toByteArray()
            );
        }
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
