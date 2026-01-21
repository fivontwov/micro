package com.dpp.ddp_study_management.service.impl;

import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.PaginationInfo;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.common.exception.AppException;
import com.dpp.ddp_study_management.common.exception.ErrorCode;
import com.dpp.ddp_study_management.dto.request.registration.MentorMenteeRegistrationCreationRequest;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMenteeResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMentorResponse;
import com.dpp.ddp_study_management.mapper.MentorMenteeRegistrationMapper;
import com.dpp.ddp_study_management.model.ERole;
import com.dpp.ddp_study_management.model.MentorMenteeRegistration;
import com.dpp.ddp_study_management.model.User;
import com.dpp.ddp_study_management.repository.MentorMenteeRegistrationRepository;
import com.dpp.ddp_study_management.repository.UserRepository;
import com.dpp.ddp_study_management.service.MentorMenteeRegistrationService;
import com.dpp.ddp_study_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MentorMenteeRegistrationServiceImpl implements MentorMenteeRegistrationService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final MentorMenteeRegistrationRepository mentorMenteeRegistrationRepository;
    private final UserRepository mentorRepository;
    private final MentorMenteeRegistrationMapper mentorMenteeRegistrationMapper;

    @Override
    @Transactional
    public MentorMenteeRegistrationForAdminResponse createMMRgs(MentorMenteeRegistrationCreationRequest request) {
        verifyMentorRegistrationRequestDate(request);

        User currentUser = userService.getCurrentUser();
        User mentee = userRepository.findByIdAndRoles_Name(currentUser.getId(), ERole.MENTEE)
                .orElseThrow(() -> new AppException(ErrorCode.IS_NOT_MENTEE));

        User mentor = mentorRepository.findByIdAndRoles_Name(request.getMentorId(), ERole.MENTOR)
                .orElseThrow(() -> new AppException(ErrorCode.MENTOR_NOT_FOUND));

        verifyMenteeNotRegisMentor(mentee.getId(), mentor.getId());

        MentorMenteeRegistration mentorMenteeRegistration
                = mentorMenteeRegistrationMapper.toEntity(request, mentor, mentee);

        mentorMenteeRegistration = mentorMenteeRegistrationRepository.save(mentorMenteeRegistration);

        return mentorMenteeRegistrationMapper.toAdminResponse(mentorMenteeRegistration);
    }

    @Override
    public PageResponse<List<MentorMenteeRegistrationForAdminResponse>> searchRegistrationsByAdmin(SearchRequest<MentorMenteeRegistrationForAdminResponse> request) {
        User currentUser = userService.getCurrentUser();
        if (!hasRole(currentUser, ERole.ADMIN)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        String searchTerm = StringUtils.hasText(request.getSearch()) ? request.getSearch().trim() : null;
        String sortBy = StringUtils.hasText(request.getName()) ? request.getName() : "id";
        String sortDirection = StringUtils.hasText(request.getSort()) ? request.getSort() : "asc";

        List<MentorMenteeRegistrationForAdminResponse> allRegistrations = mentorMenteeRegistrationRepository.searchForAdmin(searchTerm, sortBy, sortDirection);

        return paginateList(allRegistrations, request.getPage(), request.getSize());
    }

    @Override
    public PageResponse<List<MentorMenteeRegistrationForMenteeResponse>> searchRegistrationsByMentee(SearchRequest<MentorMenteeRegistrationForMenteeResponse> request) {
        User currentUser = userService.getCurrentUser();
        if (!hasRole(currentUser, ERole.MENTEE)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        String searchTerm = StringUtils.hasText(request.getSearch()) ? request.getSearch().trim() : null;
        String sortBy = StringUtils.hasText(request.getName()) ? request.getName() : "id";
        String sortDirection = StringUtils.hasText(request.getSort()) ? request.getSort() : "asc";

        List<MentorMenteeRegistrationForMenteeResponse> allRegistrations = mentorMenteeRegistrationRepository.searchForMentee(searchTerm, currentUser.getId(), sortBy, sortDirection);

        return paginateList(allRegistrations, request.getPage(), request.getSize());
    }

    @Override
    public PageResponse<List<MentorMenteeRegistrationForMentorResponse>> searchRegistrationsByMentor(SearchRequest<MentorMenteeRegistrationForMentorResponse> request) {
        User currentUser = userService.getCurrentUser();
        if (!hasRole(currentUser, ERole.MENTOR)) {
            throw new AppException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
        String searchTerm = StringUtils.hasText(request.getSearch()) ? request.getSearch().trim() : null;
        String sortBy = StringUtils.hasText(request.getName()) ? request.getName() : "id";
        String sortDirection = StringUtils.hasText(request.getSort()) ? request.getSort() : "asc";

        List<MentorMenteeRegistrationForMentorResponse> allRegistrations = mentorMenteeRegistrationRepository.searchForMentor(searchTerm, currentUser.getId(), sortBy, sortDirection);

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

    private void verifyMentorRegistrationRequestDate(MentorMenteeRegistrationCreationRequest request){
        LocalDateTime startDate = request.getStartDate();
        LocalDateTime endDate = request.getEndDate();

        if (startDate != null && startDate.isBefore(LocalDateTime.now()))
            throw new AppException(ErrorCode.START_DATE_BEFORE_NOW);

        if (endDate != null && endDate.isBefore(LocalDateTime.now()))
            throw new AppException(ErrorCode.END_DATE_BEFORE_NOW);

        if (startDate != null && endDate != null && endDate.isBefore(startDate))
            throw new AppException(ErrorCode.END_DATE_BEFORE_START_DATE);
    }

    private void verifyMenteeNotRegisMentor(Long menteeId, Long mentorId){
        MentorMenteeRegistration mmRegistration = mentorMenteeRegistrationRepository.findByMenteeIdAndMentorId(menteeId, mentorId);

        if(mmRegistration != null){
            throw new AppException(ErrorCode.MENTEE_ALREADY_REGISTERED_MENTOR);
        }
    }

}
