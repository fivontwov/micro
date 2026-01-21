package com.dpp.ddp_study_management.service.impl;

import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.PaginationInfo;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.common.exception.AppException;
import com.dpp.ddp_study_management.common.exception.ErrorCode;
import com.dpp.ddp_study_management.dto.request.user.UserCreationRequest;
import com.dpp.ddp_study_management.dto.request.user.UserUpdateRequest;
import com.dpp.ddp_study_management.dto.response.user.MentorResponse;
import com.dpp.ddp_study_management.dto.response.user.UserDetailsResponse;
import com.dpp.ddp_study_management.dto.response.user.UserResponse;
import com.dpp.ddp_study_management.model.ERole;
import com.dpp.ddp_study_management.model.Role;
import com.dpp.ddp_study_management.model.User;
import com.dpp.ddp_study_management.repository.*;
import com.dpp.ddp_study_management.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final SubjectRegistrationRepository subjectRegistrationRepository;

    private final MentorSubjectAssignmentRepository mentorSubjectAssignmentRepository;

    private final MentorMenteeRegistrationRepository mentorMenteeRegistrationRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return findByUsername(username);
    }

    @Override
    @Transactional  
    public void addUser(UserCreationRequest request) {
        validateUserCreationRequest(request);
        saveUserFromRequest(request);
    }

    @Override
    @Transactional
    public void updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isEmailExisted = userRepository.existsByEmailAndIdNot(request.getEmail(), userId);
        if (isEmailExisted) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        boolean isNameSame = request.getName().equals(user.getName());
        boolean isEmailSame = request.getEmail().equals(user.getEmail());
        boolean isDescriptionSame = request.getDescription().equals(user.getDescription());

        if (isNameSame && isEmailSame && isDescriptionSame) {
            throw new AppException(ErrorCode.USER_INFO_NOT_CHANGED);
        }

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setDescription(request.getDescription());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isAdmin = hasRole(user, ERole.ADMIN);
        boolean isMentor = hasRole(user, ERole.MENTOR);
        boolean isMentee = hasRole(user, ERole.MENTEE);

        if (isAdmin) {
            throw new AppException(ErrorCode.CANNOT_DELETE_ADMIN);
        }
        if (isMentee && (
                subjectRegistrationRepository.existsByMenteeId(userId) ||
                        mentorMenteeRegistrationRepository.existsByMenteeId(userId))
        ) {
            throw new AppException(ErrorCode.CANNOT_DELETE_MENTEE);
        }
        if (isMentor && (
                mentorSubjectAssignmentRepository.existsByMentorId(userId) ||
                        mentorMenteeRegistrationRepository.existsByMentorId(userId))
        ) {
            throw new AppException(ErrorCode.CANNOT_DELETE_MENTOR);
        }

        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public UserDetailsResponse getDetailsUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (hasRole(user, ERole.ADMIN)) {
            throw new AppException(ErrorCode.CANNOT_VIEW_ADMIN_INFO);
        }

        ERole userRole = user.getRoles().stream()
                .findFirst()
                .map(Role::getName)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        UserDetailsResponse userDetailsResponse = new UserDetailsResponse();
        userDetailsResponse.setId(user.getId());
        userDetailsResponse.setName(user.getName());
        userDetailsResponse.setUsername(user.getUsername());
        userDetailsResponse.setEmail(user.getEmail());
        userDetailsResponse.setDescription(user.getDescription());
        userDetailsResponse.setRole(userRole);
        return userDetailsResponse;
    }

    @Override
    public PageResponse<List<UserResponse>> findAllUsers(SearchRequest<User> request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSort()), request.getName());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<User> userPage = userRepository.findMentorsOrMenteesWithSearch(request.getSearch(), pageable);

        List<UserResponse> userResponses = userPage.getContent().stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());

        return getUserResponsePageResponse(userPage, userResponses);
    }

    @Override
    public PageResponse<List<MentorResponse>> findAvailableMentorsForMenteeWithSearch(SearchRequest<User> request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSort()), request.getName());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        User user = getCurrentUser();

        Page<User> userPage = userRepository.findAvailableMentorsForMenteeWithSearch(user.getId(),
                request.getSearch(), pageable);

        List<MentorResponse> mentorResponses = userPage.getContent().stream()
                .map(this::convertToMentorResponse)
                .toList();

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setPage(userPage.getNumber());
        paginationInfo.setLimit(userPage.getSize());
        paginationInfo.setTotal((int) userPage.getTotalElements());
        paginationInfo.setTotalPages(userPage.getTotalPages());

        PageResponse<List<MentorResponse>> pageResponse = new PageResponse<>();
        pageResponse.setPagination(paginationInfo);
        pageResponse.setContent(mentorResponses);
        return pageResponse;
    }

    @Override
    public PageResponse<List<MentorResponse>> findAvailableMentorsForSubjectWithSearch(Long subjectId,
                                                                                       SearchRequest<User> request) {
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSort()), request.getName());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Page<User> userPage = userRepository.findAvailableMentorsForSubjectWithSearch(subjectId,
                request.getSearch(), pageable);

        List<MentorResponse> mentorResponses = userPage.getContent().stream()
                .map(this::convertToMentorResponse)
                .toList();

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setPage(userPage.getNumber());
        paginationInfo.setLimit(userPage.getSize());
        paginationInfo.setTotal((int) userPage.getTotalElements());
        paginationInfo.setTotalPages(userPage.getTotalPages());

        PageResponse<List<MentorResponse>> pageResponse = new PageResponse<>();
        pageResponse.setPagination(paginationInfo);
        pageResponse.setContent(mentorResponses);
        return pageResponse;
    }

    private static PageResponse<List<UserResponse>> getUserResponsePageResponse(Page<User> userPage,
                                                                                List<UserResponse> userResponses) {
        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setPage(userPage.getNumber());
        paginationInfo.setLimit(userPage.getSize());
        paginationInfo.setTotal((int) userPage.getTotalElements());
        paginationInfo.setTotalPages(userPage.getTotalPages());

        PageResponse<List<UserResponse>> pageResponse = new PageResponse<>();
        pageResponse.setPagination(paginationInfo);
        pageResponse.setContent(userResponses);
        return pageResponse;
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setCreatedAt(user.getCreatedAt());

        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            ERole role = user.getRoles().iterator().next().getName();
            response.setRole(role);
        }

        return response;
    }

    private MentorResponse convertToMentorResponse(User user) {
        MentorResponse response = new MentorResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setUsername(user.getUsername());
        return  response;
    }

    private void validateUserCreationRequest(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (!roleRepository.existsByName(ERole.valueOf(request.getRole()))) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        validatePassword(request.getPassword());
    }

    private void validatePassword(String password) {
        // Check minimum length (8 characters)
        if (password.length() < 8) {
            throw new AppException(ErrorCode.PASSWORD_TOO_SHORT);
        }

        // Check maximum length (50 characters)
        if (password.length() > 50) {
            throw new AppException(ErrorCode.PASSWORD_TOO_LONG);
        }

        // Check for whitespace
        if (password.contains(" ")) {
            throw new AppException(ErrorCode.PASSWORD_CONTAINS_WHITESPACE);
        }

        // Check for at least one uppercase letter
        if (!password.matches(".*[A-Z].*")) {
            throw new AppException(ErrorCode.PASSWORD_MISSING_UPPERCASE);
        }

        // Check for at least one lowercase letter
        if (!password.matches(".*[a-z].*")) {
            throw new AppException(ErrorCode.PASSWORD_MISSING_LOWERCASE);
        }

        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            throw new AppException(ErrorCode.PASSWORD_MISSING_DIGIT);
        }

        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            throw new AppException(ErrorCode.PASSWORD_MISSING_SPECIAL_CHAR);
        }
    }

    private void saveUserFromRequest(UserCreationRequest request) {
        // TODO: Should use UserMapper to map request to user entity.
        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setDescription(request.getDescription());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        Role role = roleRepository.findByName(ERole.valueOf(request.getRole()))
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRoles(Collections.singleton(role));

        userRepository.save(user);
    }

    private boolean hasRole(User user, ERole targetRole) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(targetRole));
    }

    @Override
    public Mono<UserResponse> getUserById(Long id) {
        return Mono.fromCallable(() ->
                userRepository.findById(id)
                        .map(this::mapToResponse)
                        .orElseThrow(() -> new RuntimeException("User not found"))
        );
    }
    private UserResponse mapToResponse(User user) {
        UserResponse res = new UserResponse();
        res.setId(user.getId());
        res.setUsername(user.getUsername());
        res.setName(user.getName());
        res.setEmail(user.getEmail());
        res.setCreatedAt(user.getCreatedAt());
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            ERole role = user.getRoles().iterator().next().getName();
            res.setRole(role);
        }
        return res;
    }

}