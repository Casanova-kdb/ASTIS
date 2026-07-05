package com.astis.handbook.service;

import com.astis.handbook.dto.HandbookDraftTaskResponse;
import com.astis.task.entity.TaskPriority;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class LocalHandbookParser {

    private static final Pattern PERCENTAGE_PATTERN = Pattern.compile("(\\d{1,3})\\s*%\\s*(?:of|worth|marks?)?", Pattern.CASE_INSENSITIVE);
    private static final Pattern ISO_DATE_PATTERN = Pattern.compile("\\b(20\\d{2})[-/](\\d{1,2})[-/](\\d{1,2})\\b");
    private static final Pattern TEXT_DATE_PATTERN = Pattern.compile("\\b(\\d{1,2})\\s+(January|February|March|April|May|June|July|August|September|October|November|December)\\s+(20\\d{2})\\b", Pattern.CASE_INSENSITIVE);

    public List<HandbookDraftTaskResponse> parse(String text) {
        String title = findTitle(text);
        Integer weight = findWeight(text).orElse(null);
        LocalDateTime deadline = findDeadline(text).orElse(null);

        String description = buildDescription(text);
        Integer gradeWeight = mapWeightToSlider(weight);

        return List.of(new HandbookDraftTaskResponse(
                title,
                description,
                findTaskType(text),
                mapPriority(weight),
                deadline,
                deadline == null,
                null,
                gradeWeight,
                null,
                findDeadlineFlexibility(text),
                null,
                calculateConfidence(title, weight, deadline),
                findEvidence(text, deadline, weight)
        ));
    }

    public LocalDateTime normalizeDeadline(LocalDateTime deadline) {
        if (deadline == null) {
            return null;
        }

        if (deadline.getYear() < 2027) {
            return deadline.withYear(2027);
        }

        return deadline;
    }

    private String findTitle(String text) {
        List<String> lines = text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .limit(20)
                .toList();

        return lines.stream()
                .filter(line -> line.toLowerCase(Locale.ROOT).contains("coursework"))
                .findFirst()
                .orElseGet(() -> lines.isEmpty() ? null : lines.get(0));
    }

    private Optional<Integer> findWeight(String text) {
        Matcher matcher = PERCENTAGE_PATTERN.matcher(text);
        List<Integer> values = new ArrayList<>();

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            if (value > 0 && value <= 100) {
                values.add(value);
            }
        }

        return values.stream().max(Integer::compareTo);
    }

    private Optional<LocalDateTime> findDeadline(String text) {
        Matcher isoMatcher = ISO_DATE_PATTERN.matcher(text);
        if (isoMatcher.find()) {
            int year = Integer.parseInt(isoMatcher.group(1));
            int month = Integer.parseInt(isoMatcher.group(2));
            int day = Integer.parseInt(isoMatcher.group(3));
            return Optional.of(normalizeDeadline(LocalDateTime.of(year, month, day, 23, 59)));
        }

        Matcher textMatcher = TEXT_DATE_PATTERN.matcher(text);
        if (textMatcher.find()) {
            try {
                LocalDateTime parsed = LocalDateTime.of(
                        Integer.parseInt(textMatcher.group(3)),
                        Month.valueOf(textMatcher.group(2).toUpperCase(Locale.ROOT)),
                        Integer.parseInt(textMatcher.group(1)),
                        23,
                        59
                );
                return Optional.of(normalizeDeadline(parsed));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private String buildDescription(String text) {
        String preview = text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .skip(1)
                .limit(6)
                .reduce((left, right) -> left + " " + right)
                .orElse(null);

        if (preview != null && preview.length() > 500) {
            preview = preview.substring(0, 500).strip() + "...";
        }

        return preview;
    }

    private String findTaskType(String text) {
        String lowerText = text.toLowerCase(Locale.ROOT);

        if (lowerText.contains("coursework")) {
            return "COURSEWORK";
        }

        if (lowerText.contains("report")) {
            return "REPORT";
        }

        if (lowerText.contains("presentation")) {
            return "PRESENTATION";
        }

        if (lowerText.contains("exam")) {
            return "EXAM";
        }

        if (lowerText.contains("project")) {
            return "PROJECT";
        }

        return null;
    }

    private Integer mapWeightToSlider(Integer weight) {
        if (weight == null) {
            return null;
        }

        if (weight >= 70) {
            return 5;
        }

        if (weight >= 40) {
            return 4;
        }

        if (weight >= 20) {
            return 3;
        }

        return 2;
    }

    private TaskPriority mapPriority(Integer weight) {
        if (weight == null) {
            return null;
        }

        if (weight >= 40) {
            return TaskPriority.HIGH;
        }

        if (weight >= 20) {
            return TaskPriority.MEDIUM;
        }

        return TaskPriority.LOW;
    }

    private Integer findDeadlineFlexibility(String text) {
        String lowerText = text.toLowerCase(Locale.ROOT);

        if (lowerText.contains("no late")
                || lowerText.contains("late submissions are not")
                || lowerText.contains("late submission is not")
                || lowerText.contains("will not be accepted after")) {
            return 1;
        }

        if (lowerText.contains("late submission")
                || lowerText.contains("late submissions")
                || lowerText.contains("extension")
                || lowerText.contains("extenuating circumstances")
                || lowerText.contains("resubmission")
                || lowerText.contains("resubmit")) {
            return 5;
        }

        return null;
    }

    private int calculateConfidence(String title, Integer weight, LocalDateTime deadline) {
        int score = 25;

        if (title != null && !title.isBlank()) {
            score += 20;
        }

        if (weight != null) {
            score += 20;
        }

        if (deadline != null) {
            score += 25;
        }

        return score;
    }

    private String findEvidence(String text, LocalDateTime deadline, Integer weight) {
        return text.lines()
                .map(String::trim)
                .filter(line -> !line.isBlank())
                .filter(line -> line.toLowerCase(Locale.ROOT).contains("coursework")
                        || line.toLowerCase(Locale.ROOT).contains("due")
                        || line.toLowerCase(Locale.ROOT).contains("submission")
                        || line.contains("%"))
                .limit(4)
                .reduce((left, right) -> left + " | " + right)
                .orElse("Local parser generated this draft from the extracted handbook text.");
    }
}
