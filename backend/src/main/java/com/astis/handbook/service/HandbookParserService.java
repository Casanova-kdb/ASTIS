package com.astis.handbook.service;

import com.astis.ai.client.DeepSeekChatClient;
import com.astis.handbook.dto.HandbookDraftTaskResponse;
import com.astis.handbook.dto.HandbookParseResponse;
import com.astis.task.entity.TaskPriority;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HandbookParserService {

    private static final int MAX_AI_TEXT_CHARS = 12_000;
    private static final int MAX_PREVIEW_CHARS = 700;

    private final HandbookTextExtractionService textExtractionService;
    private final LocalHandbookParser localHandbookParser;
    private final DeepSeekChatClient deepSeekChatClient;
    private final ObjectMapper objectMapper;

    public HandbookParserService(
            HandbookTextExtractionService textExtractionService,
            LocalHandbookParser localHandbookParser,
            DeepSeekChatClient deepSeekChatClient,
            ObjectMapper objectMapper
    ) {
        this.textExtractionService = textExtractionService;
        this.localHandbookParser = localHandbookParser;
        this.deepSeekChatClient = deepSeekChatClient;
        this.objectMapper = objectMapper;
    }

    public HandbookParseResponse parse(MultipartFile file) {
        ExtractedHandbookText extracted = textExtractionService.extract(file);
        String textForAi = truncate(extracted.text(), MAX_AI_TEXT_CHARS);

        boolean fallback = !deepSeekChatClient.isConfigured();
        List<HandbookDraftTaskResponse> drafts = fallback
                ? localHandbookParser.parse(extracted.text())
                : parseWithAi(textForAi);

        if (drafts.isEmpty()) {
            fallback = true;
            drafts = localHandbookParser.parse(extracted.text());
        }

        return new HandbookParseResponse(
                extracted.filename(),
                fallback ? "local-fallback" : "deepseek",
                fallback,
                extracted.text().length(),
                truncate(extracted.text(), MAX_PREVIEW_CHARS),
                drafts
        );
    }

    private List<HandbookDraftTaskResponse> parseWithAi(String text) {
        try {
            String response = deepSeekChatClient.generateCompletion(buildSystemPrompt(), buildUserPrompt(text), 1200);
            String json = extractJsonArray(response);
            AiDraftTask[] tasks = objectMapper.readValue(json, AiDraftTask[].class);

            return Arrays.stream(tasks)
                    .map(this::normalizeAiDraft)
                    .filter(this::hasAnyExtractedValue)
                    .limit(8)
                    .toList();
        } catch (RuntimeException | java.io.IOException exception) {
            return List.of();
        }
    }

    private HandbookDraftTaskResponse normalizeAiDraft(AiDraftTask draft) {
        LocalDateTime deadline = parseDeadline(draft.deadline());
        boolean deadlineMissing = deadline == null;

        return new HandbookDraftTaskResponse(
                truncate(cleanString(draft.title()), 120),
                truncate(cleanString(draft.description()), 900),
                truncate(cleanString(draft.taskType()), 50),
                parsePriority(draft.priority()),
                deadline,
                deadlineMissing,
                positiveBigDecimalOrNull(draft.estimatedHours()),
                clampSlider(draft.gradeWeight()),
                clampSlider(draft.difficultyLevel()),
                clampSlider(draft.deadlineFlexibility()),
                clampSlider(draft.personalImportance()),
                clampConfidence(draft.confidenceScore()),
                truncate(cleanString(draft.sourceEvidence()), 500)
        );
    }

    private String buildSystemPrompt() {
        return """
                You extract academic task drafts from university module handbooks.
                Return JSON only. Do not use markdown.
                Only return fields that are clearly supported by the handbook text.
                If a field is not clearly found, return null for that field.
                Do not invent estimated hours, priority, difficulty, or importance.
                Only set deadlineFlexibility if the handbook clearly mentions late submission, extension, resubmission, or a strict no-late policy.
                If a date is before 2027, change only the year to 2027.
                If the handbook says the date is to be announced, return null for deadline.
                """;
    }

    private String buildUserPrompt(String text) {
        return """
                Extract coursework, assessment, report, exam, portfolio, presentation, and project tasks from this handbook.
                Return a JSON array. Each item must use this schema:
                {
                  "title": "string",
                  "description": "string",
                  "taskType": "COURSEWORK|REPORT|EXAM|PRESENTATION|PROJECT|READING|REVISION",
                  "priority": "LOW|MEDIUM|HIGH",
                  "deadline": "2027-06-24T23:59:00 or null",
                  "estimatedHours": 20,
                  "gradeWeight": 1-5,
                  "difficultyLevel": 1-5,
                  "deadlineFlexibility": "1 for strict/no late work, 5 for late submission or extension allowed, otherwise null",
                  "personalImportance": 1-5,
                  "confidenceScore": 0-100,
                  "sourceEvidence": "short quote or summary"
                }
                Use null for any property that is not explicitly available in the handbook text.

                Handbook text:
                %s
                """.formatted(text);
    }

    private String extractJsonArray(String value) {
        int start = value.indexOf('[');
        int end = value.lastIndexOf(']');

        if (start < 0 || end <= start) {
            return "[]";
        }

        return value.substring(start, end + 1);
    }

    private LocalDateTime parseDeadline(String value) {
        if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.strip())) {
            return null;
        }

        String trimmed = value.strip();
        try {
            return localHandbookParser.normalizeDeadline(LocalDateTime.parse(trimmed));
        } catch (DateTimeParseException exception) {
            try {
                return localHandbookParser.normalizeDeadline(LocalDateTime.of(LocalDate.parse(trimmed), LocalTime.of(23, 59)));
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private TaskPriority parsePriority(String priority) {
        if (priority == null) {
            return null;
        }

        try {
            return TaskPriority.valueOf(priority.strip().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Integer clampSlider(Integer value) {
        if (value == null) {
            return null;
        }

        return Math.max(1, Math.min(5, value));
    }

    private Integer clampConfidence(Integer value) {
        if (value == null) {
            return 50;
        }

        return Math.max(0, Math.min(100, value));
    }

    private BigDecimal positiveBigDecimalOrNull(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }

        return value;
    }

    private String cleanString(String value) {
        return value == null || value.isBlank() || "null".equalsIgnoreCase(value.strip()) ? null : value.strip();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength).strip() + "...";
    }

    private boolean hasAnyExtractedValue(HandbookDraftTaskResponse task) {
        return task.title() != null
                || task.description() != null
                || task.taskType() != null
                || task.priority() != null
                || task.deadline() != null
                || task.estimatedHours() != null
                || task.gradeWeight() != null
                || task.sourceEvidence() != null;
    }

    private record AiDraftTask(
            String title,
            String description,
            String taskType,
            String priority,
            String deadline,
            BigDecimal estimatedHours,
            Integer gradeWeight,
            Integer difficultyLevel,
            Integer deadlineFlexibility,
            Integer personalImportance,
            Integer confidenceScore,
            String sourceEvidence
    ) {
    }
}
