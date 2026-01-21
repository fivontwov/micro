package com.dpp.ddp_study_management.dto.request.subject;

import com.dpp.ddp_study_management.common.validation.annotation.MaxWords;
import com.dpp.ddp_study_management.common.validation.annotation.ValidMentorIds;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SubjectUpdateRequest {
    @NotBlank(message = "Subject name must not be blank")
    @MaxWords(max = 20, message = "Name must not exceed 20 words")
    private String name;

    @MaxWords(max = 100, message = "Description must not exceed 100 words")
    private String description;

    @ValidMentorIds(message = "Invalid mentor ID list")
    private List<Long> mentorIds;
}