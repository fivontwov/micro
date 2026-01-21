package com.dpp.ddp_study_management.common.security;

import com.dpp.ddp_study_management.model.UserDetailsImpl;
import com.dpp.ddp_study_management.service.BlacklistedTokenService;
import com.dpp.ddp_study_management.service.impl.UserDetailsServiceImpl;
import com.dpp.ddp_study_management.common.dto.ApiResponse;
import com.dpp.ddp_study_management.common.exception.ErrorCode;
import com.dpp.ddp_study_management.common.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.OutputStream;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private BlacklistedTokenService blacklistedTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        try {
            String token = jwtUtil.parseToken(request);

            if (token != null) {
                String username = jwtUtil.extractUsername(token);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                    UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtUtil.validateToken(token, userDetails) && !blacklistedTokenService.isTokenBlacklisted(token)) {

                        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()
                        );
                        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                }
            }

            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            handleJwtException(response, ErrorCode.EXPIRED_TOKEN);
        } catch (MalformedJwtException e) { //TODO: review
            handleJwtException(response, ErrorCode.INVALID_TOKEN);
        } catch (Exception e) {
            handleJwtException(response, ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void handleJwtException(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<?> apiResponse = new ApiResponse<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );

        OutputStream outputStream = response.getOutputStream();
        objectMapper.writeValue(outputStream, apiResponse);
        outputStream.flush();
    }
}