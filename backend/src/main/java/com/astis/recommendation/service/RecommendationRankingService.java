package com.astis.recommendation.service;

import com.astis.config.CacheNames;
import com.astis.recommendation.dto.RecommendedTaskResponse;
import com.astis.recommendation.model.RecommendationFeatures;
import com.astis.recommendation.model.RecommendationScore;
import com.astis.task.entity.Task;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.task.repository.UserTaskStatistics;
import com.astis.user.entity.AppUser;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecommendationRankingService {

    private final TaskRepository taskRepository;
    private final FeatureExtractionService featureExtractionService;
    private final RecommendationService recommendationService;

    public RecommendationRankingService(
            TaskRepository taskRepository,
            FeatureExtractionService featureExtractionService,
            RecommendationService recommendationService
    ) {
        this.taskRepository = taskRepository;
        this.featureExtractionService = featureExtractionService;
        this.recommendationService = recommendationService;
    }

    @Cacheable(
            cacheNames = CacheNames.USER_RECOMMENDATIONS,
            key = "#user.id"
    )
    @Transactional(readOnly = true)
    public List<RecommendedTaskResponse> rankActiveTasks(AppUser user) {
        LocalDateTime referenceTime = LocalDateTime.now();
        UserTaskStatistics taskStatistics = taskRepository.findUserTaskStatistics(
                user.getId(),
                TaskStatus.COMPLETED,
                referenceTime
        );

        List<ScoredTask> scoredTasks = taskRepository
                .findByUserIdAndStatusNotOrderByDeadlineAsc(user.getId(), TaskStatus.COMPLETED)
                .stream()
                .map(task -> {
                    RecommendationFeatures features = featureExtractionService.extractForTask(
                            user,
                            task,
                            taskStatistics,
                            referenceTime
                    );
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
