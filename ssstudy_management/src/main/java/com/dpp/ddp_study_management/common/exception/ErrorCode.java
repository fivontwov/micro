package com.dpp.ddp_study_management.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // --- 400 Bad Request ---
    BAD_REQUEST(400000, "Bad request", HttpStatus.BAD_REQUEST),
    START_DATE_BEFORE_NOW(400001, "Start date cannot be before the current date",
            HttpStatus.BAD_REQUEST),
    END_DATE_BEFORE_NOW(400002, "End date cannot be before the current date", HttpStatus.BAD_REQUEST),
    END_DATE_BEFORE_START_DATE(400003, "End date cannot be before the start date",
            HttpStatus.BAD_REQUEST),
    INVALID_MENTOR_ID(400004, "Invalid user ID or user is not a Mentor", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_SHORT(400007, "Password is too short (minimum 8 characters)", HttpStatus.BAD_REQUEST),
    PASSWORD_TOO_LONG(400008, "Password is too long (maximum 50 characters)", HttpStatus.BAD_REQUEST),
    PASSWORD_MISSING_UPPERCASE(400009, "Password must contain at least one uppercase letter",
            HttpStatus.BAD_REQUEST),
    PASSWORD_MISSING_LOWERCASE(400010, "Password must contain at least one lowercase letter",
            HttpStatus.BAD_REQUEST),
    PASSWORD_MISSING_DIGIT(400011, "Password must contain at least one digit", HttpStatus.BAD_REQUEST),
    PASSWORD_MISSING_SPECIAL_CHAR(400012, "Password must contain at least one special character",
            HttpStatus.BAD_REQUEST),
    PASSWORD_CONTAINS_WHITESPACE(400013, "Password must not contain whitespace", HttpStatus.BAD_REQUEST),
    USER_INFO_NOT_CHANGED(400014, "No changes were made", HttpStatus.BAD_REQUEST),

    // Search validation errors (400 series)
    PAGE_INVALID(400050, "Page number must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    SIZE_INVALID(400051, "Page size must be greater than 0", HttpStatus.BAD_REQUEST),
    SIZE_TOO_LARGE(400052, "Page size must be less than or equal to 100", HttpStatus.BAD_REQUEST),
    SORT_INVALID(400053, "Sort direction can only be 'asc' or 'desc'", HttpStatus.BAD_REQUEST),
    SORT_FIELD_INVALID(400054, "Invalid sort field", HttpStatus.BAD_REQUEST),

    // --- 401 Unauthorized ---
    UNAUTHORIZED_ACCESS(401000, "Unauthorized access", HttpStatus.UNAUTHORIZED),
    INVALID_PASSWORD(401001, "Invalid password", HttpStatus.UNAUTHORIZED),
    EXPIRED_TOKEN(401002, "Expired token", HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(401003, "Invalid token", HttpStatus.UNAUTHORIZED),
    IS_NOT_MENTEE(401004, "Mentee privilege required", HttpStatus.UNAUTHORIZED),
    IS_NOT_MENTOR(401005, "Mentor privilege required", HttpStatus.UNAUTHORIZED),


    // --- 403 Forbidden ---
    CANNOT_VIEW_ADMIN_INFO(403000, "Viewing detailed admin information is forbidden",
            HttpStatus.FORBIDDEN),
    ACCESS_DENIED(403001, "Access is denied", HttpStatus.FORBIDDEN),

    // --- 404 Not Found ---
    USER_NOT_FOUND(404000, "User not found", HttpStatus.NOT_FOUND),
    ROLE_NOT_FOUND(404001, "Role not found", HttpStatus.NOT_FOUND),
    MENTOR_NOT_FOUND(404002, "Mentor not found", HttpStatus.NOT_FOUND),
    SUBJECT_NOT_FOUND(404003, "Subject not found", HttpStatus.NOT_FOUND),

    // --- 409 Conflict ---
    USERNAME_ALREADY_EXISTS(409000, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_ALREADY_EXISTS(409001, "Email already exists", HttpStatus.CONFLICT),
    MENTEE_ALREADY_REGISTERED_MENTOR(409002, "Mentee has already registered for this mentor",
            HttpStatus.CONFLICT),
    MENTEE_ALREADY_REGISTERED_FOR_SUBJECT(409003, "Mentee has already registered for this subject",
            HttpStatus.CONFLICT),
    CANNOT_DELETE_ADMIN(409004, "Cannot delete ADMIN account", HttpStatus.CONFLICT),
    CANNOT_DELETE_MENTOR(409005, "Cannot delete user as it is in use in the system", HttpStatus.CONFLICT),
    CANNOT_DELETE_MENTEE(409006, "Cannot delete user as it is in use in the system", HttpStatus.CONFLICT),
    CANNOT_DELETE_SUBJECT(409007, "Cannot delete subject as it has registrants", HttpStatus.CONFLICT),


    // --- 500 Internal Server Error ---
    INTERNAL_ERROR(500000, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;
}