package com.dpp.ddp_study_management.controller;

import com.dpp.ddp_study_management.common.dto.ApiResponse;
import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.common.util.ResponseCode;
import com.dpp.ddp_study_management.dto.request.user.UserCreationRequest;
import com.dpp.ddp_study_management.dto.request.user.UserUpdateRequest;
import com.dpp.ddp_study_management.dto.response.user.MentorResponse;
import com.dpp.ddp_study_management.dto.response.user.UserDetailsResponse;
import com.dpp.ddp_study_management.dto.response.user.UserResponse;
import com.dpp.ddp_study_management.model.User;
import com.dpp.ddp_study_management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> addUser(@Valid @RequestBody UserCreationRequest request) {
        userService.addUser(request);
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                ResponseCode.USER_CREATED.getCode(),
                ResponseCode.USER_CREATED.getMessage(),
                null
        );
        return ResponseEntity
                .status(ResponseCode.USER_CREATED.getHttpStatus())
                .body(apiResponse);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        userService.updateUser(userId, request);
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                ResponseCode.USER_UPDATED.getCode(),
                ResponseCode.USER_UPDATED.getMessage(),
                null
        );
        return ResponseEntity
                .status(ResponseCode.USER_UPDATED.getHttpStatus())
                .body(apiResponse);
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        ApiResponse<Void> apiResponse = new ApiResponse<>(
                ResponseCode.USER_DELETED.getCode(),
                ResponseCode.USER_DELETED.getMessage(),
                null
        );
        return ResponseEntity
                .status(ResponseCode.USER_DELETED.getHttpStatus())
                .body(apiResponse);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailsResponse>> getUserDetails(@PathVariable Long userId) {
        ApiResponse<UserDetailsResponse> apiResponse = new ApiResponse<>(
                ResponseCode.USER_DETAILS_FETCH_SUCCESS.getCode(),
                ResponseCode.USER_DETAILS_FETCH_SUCCESS.getMessage(),
                userService.getDetailsUser(userId)
        );
        return ResponseEntity
                .status(ResponseCode.USER_DETAILS_FETCH_SUCCESS.getHttpStatus())
                .body(apiResponse);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<List<UserResponse>>>> findAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        SearchRequest<User> request = new SearchRequest<>(search, page, size, name, sort, User.class);

        PageResponse<List<UserResponse>> result = userService.findAllUsers(request);

        ApiResponse<PageResponse<List<UserResponse>>> apiResponse = new ApiResponse<>(
                ResponseCode.USER_FOUND.getCode(),
                ResponseCode.USER_FOUND.getMessage(),
                result
        );

        return ResponseEntity
                .status(ResponseCode.USER_FOUND.getHttpStatus())
                .body(apiResponse);
    }

    @GetMapping("/mentors")
    @PreAuthorize("hasRole('MENTEE')")
    public ResponseEntity<ApiResponse<PageResponse<List<MentorResponse>>>> findMentorsByRegisteredMentee(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        SearchRequest<User> request = new SearchRequest<>(search, page, size, name, sort, User.class);

        PageResponse<List<MentorResponse>> result = userService.findAvailableMentorsForMenteeWithSearch(request);

        ApiResponse<PageResponse<List<MentorResponse>>> apiResponse = new ApiResponse<>(
                ResponseCode.MENTOR_LIST_FOUND.getCode(),
                ResponseCode.MENTOR_LIST_FOUND.getMessage(),
                result
        );

        return ResponseEntity
                .status(ResponseCode.MENTOR_LIST_FOUND.getHttpStatus())
                .body(apiResponse);
    }

    @GetMapping("/mentor-subjects/{subjectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<List<MentorResponse>>>> findMentorsByRegisteredSubject(
            @PathVariable Long subjectId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        SearchRequest<User> request = new SearchRequest<>(search, page, size, name, sort, User.class);

        PageResponse<List<MentorResponse>> result = userService.findAvailableMentorsForSubjectWithSearch(subjectId,
                request);

        ApiResponse<PageResponse<List<MentorResponse>>> apiResponse = new ApiResponse<>(
                ResponseCode.MENTOR_LIST_FOUND.getCode(),
                ResponseCode.MENTOR_LIST_FOUND.getMessage(),
                result
        );

        return ResponseEntity
                .status(ResponseCode.MENTOR_LIST_FOUND.getHttpStatus())
                .body(apiResponse);
    }
}