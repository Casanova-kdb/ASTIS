package com.astis.user.controller;

import com.astis.common.api.ApiResponse;
import com.astis.user.dto.ChangePasswordRequest;
import com.astis.user.dto.UpdateCurrentUserRequest;
import com.astis.user.dto.UserResponse;
import com.astis.user.service.UserAccountService;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/me")
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @GetMapping
    public ApiResponse<UserResponse> getCurrentUser(Principal principal) {
        return ApiResponse.success("Current user retrieved", userAccountService.getCurrentUser(principal.getName()));
    }

    @PutMapping
    public ApiResponse<UserResponse> updateCurrentUser(
            Principal principal,
            @Valid @RequestBody UpdateCurrentUserRequest request
    ) {
        return ApiResponse.success("Current user updated", userAccountService.updateCurrentUser(principal.getName(), request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(
            Principal principal,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        userAccountService.changePassword(principal.getName(), request);
        return ApiResponse.success("Password updated", null);
    }
}
