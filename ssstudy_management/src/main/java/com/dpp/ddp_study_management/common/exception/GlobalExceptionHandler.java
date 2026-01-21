package com.dpp.ddp_study_management.common.exception;

import com.dpp.ddp_study_management.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<AppException>> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        ApiResponse<AppException> apiResponse = new ApiResponse<>(
                errorCode.getCode(),
                errorCode.getMessage(),
                null
        );
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<AuthenticationException>> handleAuthenticationException(AuthenticationException e) {
        ApiResponse<AuthenticationException> apiResponse = new ApiResponse<>(
                ErrorCode.INVALID_PASSWORD.getCode(),
                ErrorCode.INVALID_PASSWORD.getMessage(),
                null
        );
        return ResponseEntity
                .status(ErrorCode.INVALID_PASSWORD.getHttpStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<AccessDeniedException>> handleAccessDeniedException(AccessDeniedException e) {
        ApiResponse<AccessDeniedException> apiResponse = new ApiResponse<>(
                ErrorCode.ACCESS_DENIED.getCode(),
                ErrorCode.ACCESS_DENIED.getMessage(),
                null
        );
        return ResponseEntity
                .status(ErrorCode.ACCESS_DENIED.getHttpStatus())
                .body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ) {
        List<String> errors = new ArrayList<>();
        e.getBindingResult().getAllErrors().forEach(
                error -> errors.add( error.getDefaultMessage())
        );
        ApiResponse<Map<String, Object>> apiResponse = new ApiResponse<>(
                ErrorCode.BAD_REQUEST.getCode(),
                String.join(", ", errors),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGlobalException(Exception e) {
        e.printStackTrace();
        log.error("Unhandled exception: ", e);

        ApiResponse<Void> apiResponse = new ApiResponse<>(
                ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getMessage(),
                null
        );
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(apiResponse);
    }
}