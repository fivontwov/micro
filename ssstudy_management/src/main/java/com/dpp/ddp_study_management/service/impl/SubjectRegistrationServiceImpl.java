package com.dpp.ddp_study_management.service.impl;

import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.PaginationInfo;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.common.exception.AppException;
import com.dpp.ddp_study_management.common.exception.ErrorCode;
import com.dpp.ddp_study_management.dto.request.registration.SubjectRegistrationCreationRequest;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForMenteeResponse;
import com.dpp.ddp_study_management.mapper.SubjectRegistrationMapper;
import com.dpp.ddp_study_management.model.ERole;
import com.dpp.ddp_study_management.model.Subject;
import com.dpp.ddp_study_management.model.SubjectRegistration;
import com.dpp.ddp_study_management.model.User;
import com.dpp.ddp_study_management.repository.SubjectRegistrationRepository;
import com.dpp.ddp_study_management.repository.SubjectRepository;
import com.dpp.ddp_study_management.repository.UserRepository;
import com.dpp.ddp_study_management.service.SubjectRegistrationService;
import com.dpp.ddp_study_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubjectRegistrationServiceImpl implements SubjectRegistrationService {

    private final UserService userService;
    private final SubjectRegistrationRepository subjectRegistrationRepository;
    private final SubjectRepository subjectRepository;
    private final UserRepository userRepository;
    private final SubjectRegistrationMapper subjectRegistrationMapper;

    @Override
    @Transactional
    public SubjectRegistrationForAdminResponse createSubjectRegistration(SubjectRegistrationCreationRequest request) {
        verifySubjectRegistrationRequestDate(request);

        User currentUser = userService.getCurrentUser();
        User mentee = userRepository.findByIdAndRoles_Name(currentUser.getId(), ERole.MENTEE)
                .orElseThrow(() -> new AppException(ErrorCode.IS_NOT_MENTEE));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        verifyMenteeNotRegisteredForSubject(mentee.getId(), subject.getId());

        SubjectRegistration subjectRegistration
                = subjectRegistrationMapper.toEntity(request, subject, mentee);

        subjectRegistration = subjectRegistrationRepository.save(subjectRegistration);

        return subjectRegistrationMapper.toAdminResponse(subjectRegistration);
    }

    @Override
    public PageResponse<List<SubjectRegistrationForAdminResponse>> searchRegistrationsByAdmin(SearchRequest<SubjectRegistrationForAdminResponse> request) {
        User currentUser = userService.getCurrentUser();
        if (!hasRole(currentUser, ERole.ADMIN)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        String searchTerm = StringUtils.hasText(request.getSearch()) ? request.getSearch().trim() : null;
        String sortBy = StringUtils.hasText(request.getName()) ? request.getName() : "id";
        String sortDirection = StringUtils.hasText(request.getSort()) ? request.getSort() : "asc";

        List<SubjectRegistrationForAdminResponse> allRegistrations = subjectRegistrationRepository.searchForAdmin(searchTerm, sortBy, sortDirection);

        return paginateList(allRegistrations, request.getPage(), request.getSize());
    }

    @Override
    public boolean existsBySubjectId(Long subjectId) {
        return subjectRegistrationRepository.existsBySubjectId(subjectId);
    }

    @Override
    public PageResponse<List<SubjectRegistrationForMenteeResponse>> searchRegistrationsByMentee(SearchRequest<SubjectRegistrationForMenteeResponse> request) {
        User currentUser = userService.getCurrentUser();
        if (!hasRole(currentUser, ERole.MENTEE)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        String searchTerm = StringUtils.hasText(request.getSearch()) ? request.getSearch().trim() : null;
        String sortBy = StringUtils.hasText(request.getName()) ? request.getName() : "id";
        String sortDirection = StringUtils.hasText(request.getSort()) ? request.getSort() : "asc";

        List<SubjectRegistrationForMenteeResponse> allRegistrations = subjectRegistrationRepository.searchForMentee(searchTerm, currentUser.getId(), sortBy, sortDirection);

        return paginateList(allRegistrations, request.getPage(), request.getSize());
    }

    private <T> PageResponse<List<T>> paginateList(List<T> list, int page, int size) {
        int total = list.size();
        int totalPages = (int) Math.ceil((double) total / size);

        int fromIndex = page * size;
        if (fromIndex > total) {
            fromIndex = total;
        }
        int toIndex = Math.min(fromIndex + size, total);

        List<T> pagedContent = list.subList(fromIndex, toIndex);

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setPage(page);
        paginationInfo.setLimit(size);
        paginationInfo.setTotal(total);
        paginationInfo.setTotalPages(totalPages);

        PageResponse<List<T>> pageResponse = new PageResponse<>();
        pageResponse.setPagination(paginationInfo);
        pageResponse.setContent(pagedContent);

        return pageResponse;
    }

    private boolean hasRole(User user, ERole role) {
        return user.getRoles().stream()
                .anyMatch(r -> r.getName() == role);
    }

    private void verifySubjectRegistrationRequestDate(SubjectRegistrationCreationRequest request) {
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();

        if (startDate != null && startDate.isBefore(LocalDateTime.now()))
            throw new AppException(ErrorCode.START_DATE_BEFORE_NOW);

        if (endDate != null && endDate.isBefore(LocalDateTime.now()))
            throw new AppException(ErrorCode.END_DATE_BEFORE_NOW);

        if (startDate != null && endDate != null && endDate.isBefore(startDate))
            throw new AppException(ErrorCode.END_DATE_BEFORE_START_DATE);
    }

    private void verifyMenteeNotRegisteredForSubject(Long menteeId, Long subjectId) {
        SubjectRegistration subjectRegistration = subjectRegistrationRepository.findByMenteeIdAndSubjectId(menteeId, subjectId);

        if (subjectRegistration != null) {
            throw new AppException(ErrorCode.MENTEE_ALREADY_REGISTERED_FOR_SUBJECT);
        }
    }
}
