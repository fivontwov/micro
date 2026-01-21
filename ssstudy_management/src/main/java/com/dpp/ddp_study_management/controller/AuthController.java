package com.dpp.ddp_study_management.controller;

import com.dpp.ddp_study_management.common.dto.ApiResponse;
import com.dpp.ddp_study_management.common.util.JwtUtil;
import com.dpp.ddp_study_management.common.util.ResponseCode;
import com.dpp.ddp_study_management.dto.request.user.LoginRequest;
import com.dpp.ddp_study_management.dto.request.user.RegistrationRequest;
import com.dpp.ddp_study_management.dto.response.user.TokenResponse;
import com.dpp.ddp_study_management.service.AuthService;
import com.dpp.ddp_study_management.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final JwtUtil jwtUtil;

        private final RegistrationService registrationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        ApiResponse<TokenResponse> apiResponse = new ApiResponse<>(
                ResponseCode.LOGIN_SUCCESS,
                authService.login(loginRequest)
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(apiResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegistrationRequest request) {
        registrationService.register(request);
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                ResponseCode.USER_CREATED.getCode(),
                ResponseCode.USER_CREATED.getMessage(),
                null
        );
        return ResponseEntity
                .status(ResponseCode.USER_CREATED.getHttpStatus())
                .body(apiResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String token = jwtUtil.parseToken(request);
        authService.logout(token);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                ResponseCode.LOGOUT_SUCCESSFULLY.getCode(),
                ResponseCode.LOGOUT_SUCCESSFULLY.getMessage(),
                null
        );
        return ResponseEntity
                .status(ResponseCode.LOGOUT_SUCCESSFULLY.getHttpStatus())
                .body(apiResponse);
    }
}