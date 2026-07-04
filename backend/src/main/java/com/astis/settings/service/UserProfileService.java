package com.astis.settings.service;

import com.astis.settings.dto.UpdateUserProfileRequest;
import com.astis.settings.dto.UserProfileResponse;
import com.astis.settings.entity.UserProfile;
import com.astis.settings.repository.UserProfileRepository;
import com.astis.user.entity.AppUser;
import com.astis.user.repository.AppUserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final AppUserRepository appUserRepository;

    public UserProfileService(
            UserProfileRepository userProfileRepository,
            AppUserRepository appUserRepository
    ) {
        this.userProfileRepository = userProfileRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public UserProfileResponse getProfile(String userEmail) {
        AppUser user = findUser(userEmail);
        return UserProfileResponse.from(getOrCreateProfile(user));
    }

    @Transactional
    public UserProfileResponse updateProfile(String userEmail, UpdateUserProfileRequest request) {
        AppUser user = findUser(userEmail);
        UserProfile profile = getOrCreateProfile(user);

        profile.update(
                request.studyPace(),
                request.deadlinePressureTolerance(),
                request.dailyStudyCapacity(),
                request.preferredStudyTime(),
                request.planningStyle()
        );

        return UserProfileResponse.from(profile);
    }

    private UserProfile getOrCreateProfile(AppUser user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> userProfileRepository.save(new UserProfile(user)));
    }

    private AppUser findUser(String userEmail) {
        return appUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
