package com.astis.user.service;

import com.astis.user.dto.ChangePasswordRequest;
import com.astis.user.dto.UpdateCurrentUserRequest;
import com.astis.user.dto.UserResponse;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserAccountService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String userEmail) {
        return UserResponse.from(findUser(userEmail));
    }

    @Transactional
    public UserResponse updateCurrentUser(String userEmail, UpdateCurrentUserRequest request) {
        AppUser user = findUser(userEmail);
        user.updateProfile(request.displayName());
        return UserResponse.from(user);
    }

    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequest request) {
        AppUser user = findUser(userEmail);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect");
        }

        user.updatePasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    private AppUser findUser(String userEmail) {
        return appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
