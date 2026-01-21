package com.dpp.ddp_study_management.service;

import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.dto.request.user.UserCreationRequest;
import com.dpp.ddp_study_management.dto.request.user.UserUpdateRequest;
import com.dpp.ddp_study_management.dto.response.user.MentorResponse;
import com.dpp.ddp_study_management.dto.response.user.UserDetailsResponse;
import com.dpp.ddp_study_management.dto.response.user.UserResponse;
import com.dpp.ddp_study_management.model.User;
import reactor.core.publisher.Mono;

import java.util.List;

public interface UserService {

    User findByUsername(String username);

    User getCurrentUser();

    void addUser(UserCreationRequest request);

    void updateUser(Long userId, UserUpdateRequest request);

    void deleteUser(Long userId);

    UserDetailsResponse getDetailsUser(Long userId);

    PageResponse<List<UserResponse>> findAllUsers(SearchRequest<User> request);

    PageResponse<List<MentorResponse>> findAvailableMentorsForMenteeWithSearch(SearchRequest<User> request);

    PageResponse<List<MentorResponse>> findAvailableMentorsForSubjectWithSearch(Long subjectId,
                                                                                SearchRequest<User> request);
    Mono<UserResponse> getUserById(Long id);
}
