package com.dpp.ddp_study_management.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class RegistrationRequest {
    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Name must not be blank")
    private String name;

    @NotBlank(message = "Password must not be blank")
    private String password;

    private String email;

    private String description;

    @NotEmpty(message = "Roles must not be empty")
    private List<String> roles;
}
