package com.dpp.ddp_study_management.service.impl;

import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.PaginationInfo;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.common.exception.AppException;
import com.dpp.ddp_study_management.common.exception.ErrorCode;
import com.dpp.ddp_study_management.dto.response.subject.MentorSubjectResponse;
import com.dpp.ddp_study_management.model.ERole;
import com.dpp.ddp_study_management.model.MentorSubjectAssignment;
import com.dpp.ddp_study_management.model.Subject;
import com.dpp.ddp_study_management.model.User;
import com.dpp.ddp_study_management.repository.MentorSubjectAssignmentRepository;
import com.dpp.ddp_study_management.repository.UserRepository;
import com.dpp.ddp_study_management.service.MentorSubjectService;
import com.dpp.ddp_study_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MentorSubjectServiceImpl implements MentorSubjectService {
    private final UserRepository userRepository;
    private final MentorSubjectAssignmentRepository mentorSubjectAssignmentRepository;
    private final UserService userService;

    @Override
    @Transactional
    public void assignMentorsToSubject(Subject subject, List<Long> mentorIds) {
        if (mentorIds == null || mentorIds.isEmpty()) {
            return;
        }

        List<User> users = userRepository.findAllById(mentorIds);

        if (users.size() != mentorIds.size()) {
            throw new AppException(ErrorCode.INVALID_MENTOR_ID);
        }

        for (User user : users) {
            boolean isMentor = user.getRoles().stream()
                    .anyMatch(role -> ERole.MENTOR.equals(role.getName()));
            if (!isMentor) {
                throw new AppException(ErrorCode.INVALID_MENTOR_ID);
            }
        }

        for (User user : users) {
            MentorSubjectAssignment assignment = new MentorSubjectAssignment();
            assignment.setSubject(subject);
            assignment.setMentor(user);
            assignment.setAssignedDate(LocalDateTime.now());
            mentorSubjectAssignmentRepository.save(assignment);
        }
    }
    @Override
    @Transactional
    public void updateMentorAssignments(Subject subject, List<Long> newMentorIds) {
        if (newMentorIds == null) {
            newMentorIds = List.of();
        }

        List<User> users = userRepository.findAllById(newMentorIds);
        validateMentorIds(newMentorIds, users);
        validateUsersAreMentors(users);

        Set<Long> currentMentorIds = getCurrentMentorIds(subject.getId());
        Set<Long> newValidMentorIdsSet = new HashSet<>(newMentorIds);

        addNewMentorAssignments(subject, newValidMentorIdsSet, currentMentorIds);
        removeOldMentorAssignments(subject.getId(), newValidMentorIdsSet, currentMentorIds);
    }
    private MentorSubjectAssignment createMentorSubjectAssignment(Subject subject, User mentor) {
        MentorSubjectAssignment assignment = new MentorSubjectAssignment();
        assignment.setSubject(subject);
        assignment.setMentor(mentor);
        assignment.setAssignedDate(LocalDateTime.now());
        return assignment;
    }
    private void validateMentorIds(List<Long> mentorIds, List<User> users) {
        Set<Long> foundUserIds = users.stream().map(User::getId).collect(Collectors.toSet());

        for (Long mentorId : mentorIds) {
            if (!foundUserIds.contains(mentorId)) {
                throw new AppException(ErrorCode.USER_NOT_FOUND);
            }
        }
    }

    private void validateUsersAreMentors(List<User> users) {
        for (User user : users) {
            boolean isMentor = user.getRoles().stream()
                    .anyMatch(role -> ERole.MENTOR.equals(role.getName()));
            if (!isMentor) {
                throw new AppException(ErrorCode.INVALID_MENTOR_ID);
            }
        }
    }

    private Set<Long> getCurrentMentorIds(Long subjectId) {
        List<MentorSubjectAssignment> currentAssignments = mentorSubjectAssignmentRepository.findBySubjectId(subjectId);
        return currentAssignments.stream()
                .map(assignment -> assignment.getMentor().getId())
                .collect(Collectors.toSet());
    }

    private void addNewMentorAssignments(Subject subject, Set<Long> newMentorIds, Set<Long> currentMentorIds) {
        for (Long mentorId : newMentorIds) {
            if (!currentMentorIds.contains(mentorId)) {
                User user = userRepository.findById(mentorId)
                        .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
                MentorSubjectAssignment newAssignment = createMentorSubjectAssignment(subject, user);
                mentorSubjectAssignmentRepository.save(newAssignment);

            }
        }
    }

    private void removeOldMentorAssignments(Long subjectId, Set<Long> newMentorIds, Set<Long> currentMentorIds) {
        for (Long currentMentorId : currentMentorIds) {
            if (!newMentorIds.contains(currentMentorId)) {
                mentorSubjectAssignmentRepository.deleteBySubjectIdAndMentorId(subjectId, currentMentorId);
            }
        }
    }

    @Override
    @Transactional
    public void removeAllMentorAssignments(Subject subject) {
        List<MentorSubjectAssignment> assignments = mentorSubjectAssignmentRepository.findBySubjectId(subject.getId());
        mentorSubjectAssignmentRepository.deleteAll(assignments);
    }

    @Override
    public PageResponse<List<MentorSubjectResponse>> getSubjectsByCurrentMentor(SearchRequest<Subject> searchRequest) {
        User currentUser = userService.getCurrentUser();

        boolean isMentor = currentUser.getRoles().stream()
                .anyMatch(role -> ERole.MENTOR.equals(role.getName()));

        if (!isMentor) {
            throw new AppException(ErrorCode.IS_NOT_MENTOR);
        }

        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize());
        Page<MentorSubjectAssignment> assignmentsPage = mentorSubjectAssignmentRepository.findByMentorIdWithSearch(currentUser.getId(), searchRequest.getSearch(), pageable);

        List<MentorSubjectResponse> mentorSubjectResponses = assignmentsPage.getContent().stream()
                .map(assignment -> MentorSubjectResponse.builder()
                        .id(assignment.getSubject().getId())
                        .name(assignment.getSubject().getName())
                        .description(assignment.getSubject().getDescription())
                        .assignedDate(assignment.getAssignedDate())
                        .build())
                .collect(Collectors.toList());

        String sortField = searchRequest.getName();
        String sortDirection = searchRequest.getSort();

        if ("name".equals(sortField)) {
            if ("asc".equalsIgnoreCase(sortDirection)) {
                mentorSubjectResponses.sort(Comparator.comparing(MentorSubjectResponse::getName));
            } else {
                mentorSubjectResponses.sort(Comparator.comparing(MentorSubjectResponse::getName).reversed());
            }
        } else if ("description".equals(sortField)) {
            if ("asc".equalsIgnoreCase(sortDirection)) {
                mentorSubjectResponses.sort(Comparator.comparing(MentorSubjectResponse::getDescription));
            } else {
                mentorSubjectResponses.sort(Comparator.comparing(MentorSubjectResponse::getDescription).reversed());
            }
        }

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setPage(assignmentsPage.getNumber());
        paginationInfo.setLimit(assignmentsPage.getSize());
        paginationInfo.setTotal((int) assignmentsPage.getTotalElements());
        paginationInfo.setTotalPages(assignmentsPage.getTotalPages());

        PageResponse<List<MentorSubjectResponse>> pageResponse = new PageResponse<>();
        pageResponse.setPagination(paginationInfo);
        pageResponse.setContent(mentorSubjectResponses);

        return pageResponse;
    }
}