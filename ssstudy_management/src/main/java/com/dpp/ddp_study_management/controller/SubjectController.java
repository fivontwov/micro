package com.dpp.ddp_study_management.controller;

import com.dpp.ddp_study_management.common.dto.ApiResponse;
import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.util.ResponseCode;
import com.dpp.ddp_study_management.dto.request.subject.SubjectCreationRequest;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.dto.request.subject.SubjectUpdateRequest;
import com.dpp.ddp_study_management.dto.response.subject.MentorSubjectResponse;
import com.dpp.ddp_study_management.dto.response.subject.SubjectCreationResponse;
import com.dpp.ddp_study_management.dto.response.subject.SubjectWithMentorsResponse;
import com.dpp.ddp_study_management.model.Subject;
import com.dpp.ddp_study_management.service.MentorSubjectService;
import com.dpp.ddp_study_management.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class SubjectController {
    private final SubjectService subjectService;
    private final MentorSubjectService mentorSubjectService;
    @GetMapping("/")
    public ResponseEntity hello(){
        return ResponseEntity.ok("OK");
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<SubjectCreationResponse>> createSubject(
            @Valid @RequestBody SubjectCreationRequest subjectCreationRequest
    ) {
        subjectService.addSubject(subjectCreationRequest);
        ApiResponse<SubjectCreationResponse> apiResponse = new ApiResponse<>(
                ResponseCode.SUBJECT_CREATED_WITH_MENTOR,
                null
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> updateSubject(
            @PathVariable Long subjectId,
            @Valid @RequestBody SubjectUpdateRequest subjectUpdateRequest) {
        subjectService.updateSubject(subjectId, subjectUpdateRequest);
        ApiResponse<Void> apiResponse = new ApiResponse<>(ResponseCode.SUBJECT_UPDATED, null);
        return ResponseEntity.ok(apiResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(
            @PathVariable Long subjectId) {
        subjectService.deleteSubject(subjectId);
        ApiResponse<Void> apiResponse = new ApiResponse<>(ResponseCode.SUBJECT_DELETED, null);
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<PageResponse<List<SubjectWithMentorsResponse>> >> getSubjectsWithMentor(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort) {

        SearchRequest<Subject> searchRequest = new SearchRequest<>(search, page, size, name, sort, Subject.class);
        PageResponse<List<SubjectWithMentorsResponse>> result = subjectService.getSubjectsWithMentors(searchRequest);

        ApiResponse<PageResponse<List<SubjectWithMentorsResponse>> > apiResponse = new ApiResponse<>(
                ResponseCode.SUCCESS,
                result
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("subjects-assigned")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ApiResponse<PageResponse<List<MentorSubjectResponse>>>> getSubjectsForCurrentMentor(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort) {

        SearchRequest<Subject> searchRequest = new SearchRequest<>(search, page, size, name, sort, Subject.class);
        PageResponse<List<MentorSubjectResponse>> result = mentorSubjectService.getSubjectsByCurrentMentor(searchRequest);

        ApiResponse<PageResponse<List<MentorSubjectResponse>>> apiResponse = new ApiResponse<>(
                ResponseCode.SUCCESS,
                result
        );
        return ResponseEntity.ok(apiResponse);
    }

    @GetMapping("/subjects/unregistered")
    @PreAuthorize("hasRole('MENTEE')")
    public ResponseEntity<ApiResponse<PageResponse<List<Subject>>>> getUnregisteredSubjectsForMentee(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort) {

        SearchRequest<Subject> searchRequest = new SearchRequest<>(search, page, size, name, sort, Subject.class);
        PageResponse<List<Subject>> result = subjectService.getUnregisteredSubjectsByMenteeId(searchRequest);

        ApiResponse<PageResponse<List<Subject>>> apiResponse = new ApiResponse<>(
                ResponseCode.SUCCESS,
                result
        );
        return ResponseEntity.ok(apiResponse);
    }


}
