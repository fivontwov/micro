package com.dpp.ddp_study_management.service.impl;

import com.dpp.ddp_study_management.common.util.JwtUtil;
import com.dpp.ddp_study_management.dto.request.user.LoginRequest;
import com.dpp.ddp_study_management.dto.response.user.TokenResponse;
import com.dpp.ddp_study_management.service.AuthService;
import com.dpp.ddp_study_management.service.BlacklistedTokenService;
import com.dpp.ddp_study_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    private final BlacklistedTokenService blacklistedTokenService;

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        var user = userService.findByUsername(loginRequest.getUsername());

        // diagnostic: check raw password vs stored hash
        BCryptPasswordEncoder enc = new BCryptPasswordEncoder(12);
        boolean matches = enc.matches(loginRequest.getPassword(), user.getPassword());
        System.out.println("[DEBUG] BCrypt.matches = " + matches + " for user=" + loginRequest.getUsername());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
            ));
        } catch (AuthenticationException ex) {
            // print stacktrace to console for debugging, then rethrow so global handler maps to INVALID_PASSWORD
            ex.printStackTrace();
            throw ex;
        }

        try {
            String token = jwtUtil.generateToken(loginRequest.getUsername());
            return new TokenResponse(token);
        } catch (Exception e) {
            System.err.println("[ERROR] Failed to generate JWT token for user=" + loginRequest.getUsername());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void logout(String token) {
        blacklistedTokenService.addBlacklistedToken(token);
    }
}