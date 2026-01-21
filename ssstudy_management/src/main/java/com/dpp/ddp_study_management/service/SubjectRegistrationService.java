package com.dpp.ddp_study_management.service;

import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.dto.request.registration.SubjectRegistrationCreationRequest;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForMenteeResponse;
import java.util.List;

public interface SubjectRegistrationService {
    SubjectRegistrationForAdminResponse createSubjectRegistration(SubjectRegistrationCreationRequest request);
    PageResponse<List<SubjectRegistrationForAdminResponse>> searchRegistrationsByAdmin(SearchRequest<SubjectRegistrationForAdminResponse> request);
    PageResponse<List<SubjectRegistrationForMenteeResponse>> searchRegistrationsByMentee(SearchRequest<SubjectRegistrationForMenteeResponse> request);
    boolean existsBySubjectId(Long subjectId);
}