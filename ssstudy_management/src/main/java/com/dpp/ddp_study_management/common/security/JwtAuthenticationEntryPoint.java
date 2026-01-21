package com.dpp.ddp_study_management.common.security;

import com.dpp.ddp_study_management.common.dto.ApiResponse;
import com.dpp.ddp_study_management.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<?> apiResponse = new ApiResponse<>(
                ErrorCode.UNAUTHORIZED_ACCESS.getCode(),
                authenticationException.getMessage(),
                null
        );

        OutputStream outputStream = response.getOutputStream();
        objectMapper.writeValue(outputStream, apiResponse);
        outputStream.flush();
    }
}