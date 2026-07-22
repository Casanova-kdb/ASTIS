package com.astis.handbook.service;

import com.astis.ai.client.DeepSeekChatClient;
import com.astis.ai.config.DeepSeekProperties;
import com.astis.handbook.dto.HandbookParseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class HandbookParserServiceTests {

    private static final String HANDBOOK_TEXT = """
            Web Application Development: Coursework
            This coursework is worth 100% of the module marks.
            Submission deadline: 2026-06-24.
            """;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parseUsesLocalFallbackWhenDeepSeekIsNotConfigured() {
        HandbookParserService service = createService(new StubDeepSeekChatClient(false, null, null));

        HandbookParseResponse response = service.parse(file());

        assertThat(response.fallback()).isTrue();
        assertThat(response.provider()).isEqualTo("local-fallback");
        assertThat(response.fallbackReason()).contains("not configured");
        assertThat(response.drafts()).hasSize(1);
    }

    @Test
    void parseFallsBackWhenDeepSeekReturnsInvalidJson() {
        HandbookParserService service = createService(new StubDeepSeekChatClient(true, "not-json", null));

        HandbookParseResponse response = service.parse(file());

        assertThat(response.fallback()).isTrue();
        assertThat(response.provider()).isEqualTo("local-fallback");
        assertThat(response.fallbackReason()).contains("parsing failed");
        assertThat(response.drafts()).hasSize(1);
    }

    @Test
    void parseExplainsDeepSeekTimeoutWhenUsingLocalFallback() {
        HandbookParserService service = createService(new StubDeepSeekChatClient(
                true,
                null,
                new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, "DeepSeek request timed out")
        ));

        HandbookParseResponse response = service.parse(file());

        assertThat(response.fallback()).isTrue();
        assertThat(response.fallbackReason()).contains("DeepSeek request timed out");
        assertThat(response.drafts()).hasSize(1);
    }

    @Test
    void parsePreservesPastDeadlineReturnedByDeepSeek() {
        String aiResponse = """
                [
                  {
                    "title": "Web Application Development Coursework",
                    "taskType": "COURSEWORK",
                    "priority": "HIGH",
                    "deadline": "2026-06-24T23:59:00",
                    "confidenceScore": 95,
                    "sourceEvidence": "Submission deadline: 2026-06-24"
                  }
                ]
                """;
        HandbookParserService service = createService(new StubDeepSeekChatClient(true, aiResponse, null));

        HandbookParseResponse response = service.parse(file());

        assertThat(response.fallback()).isFalse();
        assertThat(response.provider()).isEqualTo("deepseek");
        assertThat(response.fallbackReason()).isNull();
        assertThat(response.drafts().get(0).deadline()).hasToString("2026-06-24T23:59");
    }

    private HandbookParserService createService(DeepSeekChatClient client) {
        return new HandbookParserService(
                new StubHandbookTextExtractionService(),
                new LocalHandbookParser(),
                client,
                objectMapper
        );
    }

    private MockMultipartFile file() {
        return new MockMultipartFile(
                "file",
                "handbook.pdf",
                "application/pdf",
                "placeholder".getBytes(StandardCharsets.UTF_8)
        );
    }

    private static class StubHandbookTextExtractionService extends HandbookTextExtractionService {
        @Override
        public ExtractedHandbookText extract(org.springframework.web.multipart.MultipartFile file) {
            return new ExtractedHandbookText(file.getOriginalFilename(), HANDBOOK_TEXT);
        }
    }

    private static class StubDeepSeekChatClient extends DeepSeekChatClient {
        private final boolean configured;
        private final String response;
        private final RuntimeException failure;

        private StubDeepSeekChatClient(boolean configured, String response, RuntimeException failure) {
            super(
                    new DeepSeekProperties(
                            configured,
                            "https://api.deepseek.com",
                            configured ? "test-key" : "",
                            "test-model",
                            0.3,
                            600,
                            6000,
                            60
                    ),
                    new ObjectMapper()
            );
            this.configured = configured;
            this.response = response;
            this.failure = failure;
        }

        @Override
        public boolean isConfigured() {
            return configured;
        }

        @Override
        public int handbookMaxTokens() {
            return 6000;
        }

        @Override
        public String generateCompletion(String systemPrompt, String prompt, int maxTokens) {
            if (failure != null) {
                throw failure;
            }
            return response;
        }
    }
}
