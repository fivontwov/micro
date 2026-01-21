package com.dpp.ddp_study_management.dto.request.user;

import com.dpp.ddp_study_management.common.validation.annotation.ValidRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserCreationRequest {

    @NotBlank(message = "Username must not be blank")
    private String username;

    @NotBlank(message = "Display name must not be blank")
    private String name;

    @NotBlank(message = "Password must not be blank")
    private String password;

    @Email(message = "Email must be a valid email address")
    private String email;

    private String description;

    @NotBlank(message = "Role must not be blank")
    @ValidRole
    private String role;
}