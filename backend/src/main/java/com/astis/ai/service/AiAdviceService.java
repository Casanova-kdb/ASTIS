package com.astis.ai.service;

import com.astis.ai.client.DeepSeekChatClient;
import com.astis.ai.dto.AiStudyAdviceResponse;
import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.astis.recommendation.service.RecommendationQueryService;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AiAdviceService {

    private static final int MAX_TASKS_FOR_ADVICE = 3;

    private final RecommendationQueryService recommendationQueryService;
    private final DeepSeekChatClient deepSeekChatClient;

    public AiAdviceService(
            RecommendationQueryService recommendationQueryService,
            DeepSeekChatClient deepSeekChatClient
    ) {
        this.recommendationQueryService = recommendationQueryService;
        this.deepSeekChatClient = deepSeekChatClient;
    }

    public AiStudyAdviceResponse generateStudyAdvice(String userEmail) {
        List<RecommendedTaskResponse> recommendedTasks = recommendationQueryService.getRecommendedTasks(userEmail)
                .stream()
                .limit(MAX_TASKS_FOR_ADVICE)
                .toList();

        boolean fallback = !deepSeekChatClient.isConfigured();
        String advice = fallback
                ? buildFallbackAdvice(recommendedTasks)
                : deepSeekChatClient.generateAdvice(buildPrompt(recommendedTasks));

        return new AiStudyAdviceResponse(
                fallback ? "local-fallback" : "deepseek",
                deepSeekChatClient.model(),
                fallback,
                advice,
                recommendedTasks,
                LocalDateTime.now()
        );
    }

    private String buildPrompt(List<RecommendedTaskResponse> recommendedTasks) {
        if (recommendedTasks.isEmpty()) {
            return "The user has no active recommended tasks. Give a short study planning suggestion.";
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a concise study plan based on these ranked tasks. ");
        prompt.append("Explain what to do first, why, and one risk-reduction suggestion. ");
        prompt.append("Keep the response under 180 words.\n\n");

        for (RecommendedTaskResponse task : recommendedTasks) {
            prompt.append("Rank ").append(task.rankPosition()).append(": ")
                    .append(task.title())
                    .append(" | type=").append(task.taskType())
                    .append(" | deadline=").append(task.deadline())
                    .append(" | score=").append(task.priorityScore())
                    .append(" | delayRisk=").append(task.delayRisk())
                    .append(" | reason=").append(task.reason())
                    .append("\n");
        }

        return prompt.toString();
    }

    private String buildFallbackAdvice(List<RecommendedTaskResponse> recommendedTasks) {
        if (recommendedTasks.isEmpty()) {
            return "No active tasks are currently available. Use this time to review upcoming deadlines and add any missing study tasks.";
        }

        RecommendedTaskResponse firstTask = recommendedTasks.get(0);
        return "Start with \"%s\" because it is ranked first with a priority score of %.2f and %s delay risk. %s"
                .formatted(
                        firstTask.title(),
                        firstTask.priorityScore(),
                        firstTask.delayRisk().toLowerCase(),
                        "After that, review the next recommended task and keep the session focused on one clear deliverable."
                );
    }
}
