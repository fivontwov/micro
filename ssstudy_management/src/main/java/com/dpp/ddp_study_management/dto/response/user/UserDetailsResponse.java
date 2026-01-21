package com.dpp.ddp_study_management.dto.response.user;

import com.dpp.ddp_study_management.model.ERole;
import lombok.Data;

@Data
public class UserDetailsResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private ERole role;
    private String description;
}