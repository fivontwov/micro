package com.dpp.ddp_study_management.service;

import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.dto.request.subject.SubjectCreationRequest;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.dto.request.subject.SubjectUpdateRequest;
import com.dpp.ddp_study_management.dto.response.subject.SubjectResponse;
import com.dpp.ddp_study_management.dto.response.subject.SubjectWithMentorsResponse;
import com.dpp.ddp_study_management.model.Subject;

import java.util.List;

public interface SubjectService {
    void addSubject(SubjectCreationRequest request);
    void updateSubject(Long subjectId, SubjectUpdateRequest request);
    void deleteSubject(Long subjectId);
    PageResponse<List<SubjectWithMentorsResponse>> getSubjectsWithMentors(SearchRequest<Subject> searchRequest);
    PageResponse<List<Subject>> getUnregisteredSubjectsByMenteeId(SearchRequest<Subject> searchRequest);

}