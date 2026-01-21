package com.dpp.ddp_study_management.service.impl;

import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.PaginationInfo;
import com.dpp.ddp_study_management.common.exception.AppException;
import com.dpp.ddp_study_management.common.exception.ErrorCode;
import com.dpp.ddp_study_management.dto.request.subject.SubjectCreationRequest;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.dto.request.subject.SubjectUpdateRequest;
import com.dpp.ddp_study_management.dto.response.subject.SubjectResponse;
import com.dpp.ddp_study_management.dto.response.user.MentorResponse;
import com.dpp.ddp_study_management.dto.response.subject.SubjectWithMentorsResponse;
import com.dpp.ddp_study_management.model.Subject;
import com.dpp.ddp_study_management.repository.SubjectRepository;
import com.dpp.ddp_study_management.service.MentorSubjectService;
import com.dpp.ddp_study_management.service.SubjectRegistrationService;
import com.dpp.ddp_study_management.service.SubjectService;
import com.dpp.ddp_study_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class SubjectServiceImpl implements SubjectService {
    private final SubjectRepository subjectRepository;
    private final MentorSubjectService mentorSubjectService;
    private final SubjectRegistrationService subjectRegistrationService;
    private final UserService userService;

    @Override
    @Transactional
    @CacheEvict(value = {"subjects"}, allEntries = true)
    public void addSubject(SubjectCreationRequest request) {
        Subject newSubject = new Subject();
        newSubject.setName(request.getName());
        newSubject.setDescription(request.getDescription());
        Subject savedSubject = subjectRepository.save(newSubject);
        List<Long> mentorIds = request.getMentorIds();

        mentorSubjectService.assignMentorsToSubject(savedSubject, mentorIds);
    }
    @Override
    @Transactional
    @CacheEvict(value = {"subjects"}, allEntries = true)
    public void updateSubject(Long subjectId, SubjectUpdateRequest request) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        subject.setName(request.getName());
        subject.setDescription(request.getDescription());
        subjectRepository.save(subject);

        mentorSubjectService.updateMentorAssignments(subject, request.getMentorIds());
    }

    @Override
    @Transactional
    @CacheEvict(value = {"subjects"}, allEntries = true)
    public void deleteSubject(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new AppException(ErrorCode.SUBJECT_NOT_FOUND));

        if (subjectRegistrationService.existsBySubjectId(subjectId)) {
            throw new AppException(ErrorCode.CANNOT_DELETE_SUBJECT);
        }

        mentorSubjectService.removeAllMentorAssignments(subject);
        subjectRepository.delete(subject);
    }

    @Override
    @Transactional
    @Cacheable(value = "subjects", key = "#searchRequest.search + '_' + #searchRequest.page + '_' + #searchRequest.size + '_' + #searchRequest.sort + '_' + #searchRequest.name")
    public PageResponse<List<SubjectWithMentorsResponse>> getSubjectsWithMentors(SearchRequest<Subject> searchRequest) {
        Sort sort = Sort.by(Sort.Direction.fromString(searchRequest.getSort()), searchRequest.getName());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        Page<Subject> subjectPage;
        if (searchRequest.getSearch() == null || searchRequest.getSearch().isBlank()) {
            subjectPage = subjectRepository.findAllWithMentors(pageable);
        } else {
            subjectPage = subjectRepository.findByNameContainingIgnoreCaseWithMentors(searchRequest.getSearch(), pageable);
        }

        List<SubjectWithMentorsResponse> data = subjectPage.getContent().stream()
                .map(this::convertToSubjectWithMentorsResponse)
                .collect(Collectors.toList());

        PaginationInfo paginationInfo = new PaginationInfo();
        paginationInfo.setPage(subjectPage.getNumber());
        paginationInfo.setLimit(subjectPage.getSize());
        paginationInfo.setTotal((int) subjectPage.getTotalElements());
        paginationInfo.setTotalPages(subjectPage.getTotalPages());

        PageResponse<List<SubjectWithMentorsResponse>> response = new PageResponse<>();
        response.setPagination(paginationInfo);
        response.setContent(data);

        return response;
    }

    private SubjectWithMentorsResponse convertToSubjectWithMentorsResponse(Subject subject) {
        SubjectWithMentorsResponse response = new SubjectWithMentorsResponse();
        response.setId(subject.getId());
        response.setName(subject.getName());
        response.setDescription(subject.getDescription());

        List<MentorResponse> mentorResponses = subject.getMentorSubjectAssignedList().stream()
                .filter(assignment -> assignment.getMentor() != null)
                .map(assignment -> {
                    MentorResponse mentorResponse = new MentorResponse();
                    mentorResponse.setId(assignment.getMentor().getId());
                    mentorResponse.setUsername(assignment.getMentor().getUsername());
                    mentorResponse.setName(assignment.getMentor().getName());
                    return mentorResponse;
                })
                .collect(Collectors.toList());

        response.setMentors(mentorResponses);
        return response;
    }


    @Override
    @Transactional
    public PageResponse<List<Subject>> getUnregisteredSubjectsByMenteeId(SearchRequest<Subject> searchRequest) {
        Sort sort = Sort.by(Sort.Direction.fromString(searchRequest.getSort()), searchRequest.getName());
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        List<Subject> subjectPage = subjectRepository.findUnregisteredSubjectsByMenteeId(userService.getCurrentUser().getId());
//        List<Subject> data = subjectPage.getContent().stream()
//                .collect(Collectors.toList());
        PaginationInfo paginationInfo = new PaginationInfo();
//        paginationInfo.setPage(subjectPage.getNumber());
//        paginationInfo.setLimit(subjectPage.getSize());
//        paginationInfo.setTotal((int) subjectPage.getTotalElements());
//        paginationInfo.setTotalPages(subjectPage.getTotalPages());

        PageResponse<List<Subject>> response = new PageResponse<>();
        //response.setPagination(paginationInfo);
        response.setContent(subjectPage);

        return response;
    }
}