package com.astis.analytics.service;

import com.astis.analytics.dto.AnalyticsSummaryResponse;
import com.astis.task.entity.TaskStatus;
import com.astis.task.repository.TaskRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AnalyticsService {

    private final TaskRepository taskRepository;
    private final AppUserRepository appUserRepository;

    public AnalyticsService(TaskRepository taskRepository, AppUserRepository appUserRepository) {
        this.taskRepository = taskRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public AnalyticsSummaryResponse getSummary(String userEmail) {
        AppUser user = appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));

        long totalTaskCount = taskRepository.countByUserId(user.getId());
        long completedTaskCount = taskRepository.countByUserIdAndStatus(user.getId(), TaskStatus.COMPLETED);
        long overdueTaskCount = taskRepository.countByUserIdAndStatusNotAndDeadlineBefore(
                user.getId(),
                TaskStatus.COMPLETED,
                LocalDateTime.now()
        );
        double completionRate = totalTaskCount == 0 ? 0.0 : (double) completedTaskCount / totalTaskCount;

        return new AnalyticsSummaryResponse(
                totalTaskCount,
                completedTaskCount,
                overdueTaskCount,
                completionRate
        );
    }
}
