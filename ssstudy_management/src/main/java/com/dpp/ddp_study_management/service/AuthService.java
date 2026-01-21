package com.dpp.ddp_study_management.service;

import com.dpp.ddp_study_management.dto.request.user.LoginRequest;
import com.dpp.ddp_study_management.dto.response.user.TokenResponse;

public interface AuthService {
    TokenResponse login(LoginRequest loginRequest);
    void logout(String token);
}