package com.dpp.ddp_study_management.dto.request.registration;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SubjectRegistrationCreationRequest {

    @NotNull(message = "Subject ID must not be null.")
    @Positive(message = "Subject ID must be a positive number.")
    private Long subjectId;

    @NotNull(message = "Start date must not be null.")
    @FutureOrPresent(message = "Start date must be in the present or future.")
    private LocalDateTime startDate;

    @NotNull(message = "End date must not be null.")
    @FutureOrPresent(message = "End date must be in the present or future.")
    private LocalDateTime endDate;
}