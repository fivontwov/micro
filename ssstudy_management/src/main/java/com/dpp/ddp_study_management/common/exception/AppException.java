package com.dpp.ddp_study_management.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
}