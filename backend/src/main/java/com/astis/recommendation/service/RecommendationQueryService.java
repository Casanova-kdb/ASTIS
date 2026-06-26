package com.astis.recommendation.service;

import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.astis.recommendation.model.RecommendationFeatures;
import com.astis.recommendation.model.RecommendationScore;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecommendationQueryService {

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;
    private final FeatureExtractionService featureExtractionService;
    private final RecommendationService recommendationService;

    public RecommendationQueryService(
            TaskRepository taskRepository,
            AppUserRepository appUserRepository,
            FeatureExtractionService featureExtractionService,
            RecommendationService recommendationService
    ) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
        this.featureExtractionService = featureExtractionService;
        this.recommendationService = recommendationService;
    }

    @Transactional(readOnly = true)
    public List<RecommendedTaskResponse> getRecommendedTasks(String userEmail) {
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        List<ScoredTask> scoredTasks = taskRepository
                .findByUserIdAndStatusNotOrderByDeadlineAsc(user.getId(), TaskStatus.COMPLETED)
                .stream()
                .map(task -> {
                    RecommendationFeatures features = featureExtractionService.extractForTask(userEmail, task.getId());
                    RecommendationScore score = recommendationService.scoreTask(features);
                    return new ScoredTask(task, score);
                })
                .sorted(Comparator
                        .comparing((ScoredTask scoredTask) -> scoredTask.score().priorityScore())
                        .reversed()
                        .thenComparing(scoredTask -> scoredTask.task().getDeadline()))
                .toList();

        return toRankedResponses(scoredTasks);
    }

    private List<RecommendedTaskResponse> toRankedResponses(List<ScoredTask> scoredTasks) {
        return java.util.stream.IntStream.range(0, scoredTasks.size())
                .mapToObj(index -> RecommendedTaskResponse.from(
                        index + 1,
                        scoredTasks.get(index).task(),
                        scoredTasks.get(index).score()
                ))
                .toList();
    }

    private record ScoredTask(Task task, RecommendationScore score) {
    }
}
