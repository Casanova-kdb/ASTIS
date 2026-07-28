package com.astis.studyplan.controller;

import com.astis.common.api.ApiResponse;
import com.astis.studyplan.dto.StudyPlanResponse;
import com.astis.studyplan.service.StudyPlanQueryService;
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
@RequestMapping("/study-plans")
@Tag(name = "Study Plans", description = "Generate a study schedule from the current user's recommended tasks")
@SecurityRequirement(name = "bearerAuth")
public class StudyPlanController {

    private final StudyPlanQueryService studyPlanQueryService;

    public StudyPlanController(StudyPlanQueryService studyPlanQueryService) {
        this.studyPlanQueryService = studyPlanQueryService;
    }

    @GetMapping
    @Operation(
            summary = "Generate a study plan",
            description = "Creates a non-persisted study plan for the authenticated user over a 1-to-14-day window."
    )
    public ApiResponse<StudyPlanResponse> getStudyPlan(
            Principal principal,
            @Parameter(
                    description = "Number of calendar days to include",
                    schema = @Schema(
                            type = "integer",
                            defaultValue = "7",
                            minimum = "1",
                            maximum = "14"
                    )
            )
            @RequestParam(defaultValue = "7") int days
    ) {
        return ApiResponse.success(
                "Study plan generated",
                studyPlanQueryService.generateStudyPlan(principal.getName(), days)
        );
    }
}
