package com.dpp.ddp_study_management.dto.request.subject;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.List;

@Data
public class SubjectRequest {
    @NotBlank(message = "Subject name must not be blank")
    private String name;
    private String description;
    private List<Long> mentorIds;
}
