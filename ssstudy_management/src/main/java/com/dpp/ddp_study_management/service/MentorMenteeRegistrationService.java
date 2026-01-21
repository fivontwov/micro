package com.dpp.ddp_study_management.service;

import com.dpp.ddp_study_management.dto.request.registration.MentorMenteeRegistrationCreationRequest;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMenteeResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMentorResponse;

import java.util.List;

public interface MentorMenteeRegistrationService {
    MentorMenteeRegistrationForAdminResponse createMMRgs(MentorMenteeRegistrationCreationRequest request);
    PageResponse<List<MentorMenteeRegistrationForAdminResponse>> searchRegistrationsByAdmin(SearchRequest<MentorMenteeRegistrationForAdminResponse> request);
    PageResponse<List<MentorMenteeRegistrationForMenteeResponse>> searchRegistrationsByMentee(SearchRequest<MentorMenteeRegistrationForMenteeResponse> request);
    PageResponse<List<MentorMenteeRegistrationForMentorResponse>> searchRegistrationsByMentor(SearchRequest<MentorMenteeRegistrationForMentorResponse> request);
}
