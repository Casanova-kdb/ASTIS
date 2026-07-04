package com.astis.settings.controller;

import com.astis.common.api.ApiResponse;
import com.astis.settings.dto.UpdateUserProfileRequest;
import com.astis.settings.dto.UserProfileResponse;
import com.astis.settings.service.UserProfileService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/settings/profile")
public class UserProfileController {

    private final UserProfileService userProfileService;

    public UserProfileController(UserProfileService userProfileService) {
        this.userProfileService = userProfileService;
    }

    @GetMapping
    public ApiResponse<UserProfileResponse> getProfile(Principal principal) {
        return ApiResponse.success("User profile settings retrieved", userProfileService.getProfile(principal.getName()));
    }

    @PutMapping
    public ApiResponse<UserProfileResponse> updateProfile(
            Principal principal,
            @Valid @RequestBody UpdateUserProfileRequest request
    ) {
        return ApiResponse.success("User profile settings updated", userProfileService.updateProfile(principal.getName(), request));
    }
}
