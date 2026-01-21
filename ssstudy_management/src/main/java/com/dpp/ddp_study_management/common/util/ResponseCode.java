package com.dpp.ddp_study_management.common.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ResponseCode {

    // --- 200 OK ---
    SUCCESS(200, "Success", HttpStatus.OK),
    LOGIN_SUCCESS(200, "Login successful", HttpStatus.OK),
    USER_UPDATED(200, "User information updated successfully", HttpStatus.OK),
    USER_DELETED(200, "User deleted successfully", HttpStatus.OK),
    USER_DETAILS_FETCH_SUCCESS(200, "Get user details successfully", HttpStatus.OK),
    USER_FOUND(200, "User found successfully", HttpStatus.OK),
    MENTOR_LIST_FOUND(200, "Mentor list found successfully", HttpStatus.OK),
    LOGOUT_SUCCESSFULLY(200, "Logout successful", HttpStatus.OK),
    SUBJECT_UPDATED(200, "Subject information updated successfully", HttpStatus.OK),
    SUBJECT_DELETED(200, "Subject deleted successfully", HttpStatus.OK),

    // --- 201 Created ---
    USER_CREATED(201, "User created successfully", HttpStatus.CREATED),
    MENTOR_MENTEE_REGISTRATION_CREATED(201, "Mentor-Mentee registration created successfully",
            HttpStatus.CREATED),
    SUBJECT_REGISTRATION_CREATED(201, "Subject registration created successfully", HttpStatus.CREATED),
    SUBJECT_CREATED_WITH_MENTOR(201, "Subject created and mentor assigned successfully",
            HttpStatus.CREATED);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}
