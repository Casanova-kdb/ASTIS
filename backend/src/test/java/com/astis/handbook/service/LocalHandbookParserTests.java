package com.astis.handbook.service;

import com.astis.handbook.dto.HandbookDraftTaskResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocalHandbookParserTests {

    private final LocalHandbookParser parser = new LocalHandbookParser();

    @Test
    void parseCreatesDraftAndMovesPastCourseworkDateTo2027() {
        String text = """
                Web Application Development: Coursework
                This coursework is worth 100% of the marks for module CHC5054.
                Submission deadline: 2026-06-24.
                Build and test a full-stack web application.
                """;

        List<HandbookDraftTaskResponse> drafts = parser.parse(text);

        assertThat(drafts).hasSize(1);
        HandbookDraftTaskResponse draft = drafts.get(0);
        assertThat(draft.title()).isEqualTo("Web Application Development: Coursework");
        assertThat(draft.priority()).hasToString("HIGH");
        assertThat(draft.deadline()).isEqualTo(LocalDateTime.of(2027, 6, 24, 23, 59));
        assertThat(draft.deadlineMissing()).isFalse();
        assertThat(draft.gradeWeight()).isEqualTo(5);
        assertThat(draft.estimatedHours()).isNull();
        assertThat(draft.difficultyLevel()).isNull();
    }

    @Test
    void parseMarksDeadlineMissingWhenHandbookHasNoClearDate() {
        String text = """
                Web Application Development: Coursework
                Due Date: Preliminary report: Date to be announced.
                The preliminary report is worth 20% of the marks.
                """;

        HandbookDraftTaskResponse draft = parser.parse(text).get(0);

        assertThat(draft.deadline()).isNull();
        assertThat(draft.deadlineMissing()).isTrue();
        assertThat(draft.description()).doesNotContain("Please confirm it before creating the task.");
    }

    @Test
    void parseOnlySetsDeadlineFlexibilityWhenLateSubmissionPolicyExists() {
        String text = """
                Web Application Development: Coursework
                This coursework is worth 100% of the marks.
                Late submissions may be accepted through the extension process.
                """;

        HandbookDraftTaskResponse draft = parser.parse(text).get(0);

        assertThat(draft.deadlineFlexibility()).isEqualTo(5);
        assertThat(draft.difficultyLevel()).isNull();
    }
}
