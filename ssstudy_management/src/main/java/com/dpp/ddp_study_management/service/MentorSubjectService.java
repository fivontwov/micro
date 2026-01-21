package com.dpp.ddp_study_management.service;

import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.dto.response.subject.MentorSubjectResponse;
import com.dpp.ddp_study_management.model.MentorSubjectAssignment;
import com.dpp.ddp_study_management.model.Subject;

import java.util.List;

public interface MentorSubjectService {
    void assignMentorsToSubject(Subject subject, List<Long> mentorIds);
    void updateMentorAssignments(Subject subject, List<Long> newMentorIds);
    void removeAllMentorAssignments(Subject subject);
    PageResponse<List<MentorSubjectResponse>> getSubjectsByCurrentMentor(SearchRequest<Subject> searchRequest);
}
