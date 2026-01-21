package com.fivontwov.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String role;
    private String createdAt;

    public static UserDTO fromGrpcResponse(com.fivontwov.user.proto.UserResponse response) {
        UserDTO dto = new UserDTO();
        dto.setId(response.getId());
        dto.setUsername(response.getUsername());
        dto.setName(response.getName());
        dto.setEmail(response.getEmail());
        dto.setRole(response.getRole());
        dto.setCreatedAt(response.getCreatedAt());
        return dto;
    }
}
