package com.sdia.incidents.controller;

import com.sdia.incidents.dto.*;
import com.sdia.incidents.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public UserResponse getProfile() {
        return profileService.getProfile();
    }

    @PutMapping
    public ProfileUpdateResponse updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        return profileService.updateProfile(request);
    }

    @PutMapping("/password")
    public MessageResponse changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        return profileService.changePassword(request);
    }
}
