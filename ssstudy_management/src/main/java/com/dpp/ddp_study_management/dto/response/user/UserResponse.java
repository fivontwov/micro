package com.dpp.ddp_study_management.dto.response.user;

import com.dpp.ddp_study_management.model.ERole;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private ERole role;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}