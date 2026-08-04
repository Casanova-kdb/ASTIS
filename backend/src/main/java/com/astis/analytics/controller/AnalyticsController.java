package com.astis.analytics.controller;

import com.astis.analytics.dto.AnalyticsSummaryResponse;
import com.astis.analytics.dto.AnalyticsTrendResponse;
import com.astis.analytics.service.AnalyticsService;
import com.astis.analytics.service.AnalyticsTrendService;
import com.astis.common.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
@Tag(name = "Analytics", description = "View task summaries and weekly productivity trends")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final AnalyticsTrendService analyticsTrendService;

    public AnalyticsController(
            AnalyticsService analyticsService,
            AnalyticsTrendService analyticsTrendService
    ) {
        this.analyticsService = analyticsService;
        this.analyticsTrendService = analyticsTrendService;
    }

    @GetMapping("/summary")
    @Operation(summary = "Get the current task summary")
    public ApiResponse<AnalyticsSummaryResponse> getSummary(Principal principal) {
        return ApiResponse.success("Analytics summary retrieved", analyticsService.getSummary(principal.getName()));
    }

    @GetMapping("/trends")
    @Operation(
            summary = "Get weekly productivity trends",
            description = "Returns completion and overdue trends for the authenticated user over a 4-to-12-week window."
    )
    public ApiResponse<AnalyticsTrendResponse> getTrends(
            Principal principal,
            @Parameter(
                    description = "Number of calendar weeks to include",
                    schema = @Schema(
                            type = "integer",
                            defaultValue = "8",
                            minimum = "4",
                            maximum = "12"
                    )
            )
            @RequestParam(defaultValue = "8") int weeks
    ) {
        return ApiResponse.success(
                "Analytics trends retrieved",
                analyticsTrendService.getTrends(principal.getName(), weeks)
        );
    }
}
