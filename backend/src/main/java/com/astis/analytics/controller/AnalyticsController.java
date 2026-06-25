package com.astis.analytics.controller;

import com.astis.analytics.dto.AnalyticsSummaryResponse;
import com.astis.analytics.service.AnalyticsService;
import com.astis.common.api.ApiResponse;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/summary")
    public ApiResponse<AnalyticsSummaryResponse> getSummary(Principal principal) {
        return ApiResponse.success("Analytics summary retrieved", analyticsService.getSummary(principal.getName()));
    }
}
