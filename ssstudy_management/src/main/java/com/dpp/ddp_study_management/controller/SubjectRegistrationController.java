package com.dpp.ddp_study_management.controller;

import com.dpp.ddp_study_management.common.dto.ApiResponse;
import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.common.util.ResponseCode;
import com.dpp.ddp_study_management.dto.request.registration.SubjectRegistrationCreationRequest;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForMenteeResponse;
import com.dpp.ddp_study_management.service.SubjectRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject-registrations")
@RequiredArgsConstructor
@Validated
public class SubjectRegistrationController {

    private final SubjectRegistrationService subjectRegistrationService;

    /**
     * Tạo mới đăng ký môn học cho mentee
     *
     * @param request : đối tượng chứa thông tin đăng ký
     * @return ApiResponse chứa đăng ký môn học đã được tạo
     */
    @PostMapping("")
    @PreAuthorize("hasRole('MENTEE')")
    public ResponseEntity<ApiResponse<SubjectRegistrationForAdminResponse>> createSubjectRegistration(
            @Valid @RequestBody SubjectRegistrationCreationRequest request) {

        SubjectRegistrationForAdminResponse result = subjectRegistrationService.createSubjectRegistration(request);

        ApiResponse<SubjectRegistrationForAdminResponse> response = new ApiResponse<>(
                ResponseCode.SUBJECT_REGISTRATION_CREATED,
                result
        );

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Search API cho Admin
     *
     * @param search Từ khóa tìm kiếm chung
     * @param page Số thứ tự trang (bắt đầu từ 0)
     * @param size Số phần tử trên trang
     * @param name Tên trường sắp xếp
     * @param sort Chiều sắp xếp (asc, desc)
     * @return PageResponse<List<SubjectRegistrationForAdminResponse>>
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<List<SubjectRegistrationForAdminResponse>>>> searchRegistrationsByAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort) {

        SearchRequest<SubjectRegistrationForAdminResponse> searchRequest
                = new SearchRequest<>(search, page, size, name, sort, SubjectRegistrationForAdminResponse.class);

        PageResponse<List<SubjectRegistrationForAdminResponse>> result =
                subjectRegistrationService.searchRegistrationsByAdmin(searchRequest);

        ApiResponse<PageResponse<List<SubjectRegistrationForAdminResponse>>> response = new ApiResponse<>(
                ResponseCode.SUCCESS,
                result
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Search API cho Mentee
     *
     * @param search Từ khóa tìm kiếm chung
     * @param page Số thứ tự trang (bắt đầu từ 0)
     * @param size Số phần tử trên trang
     * @param name Tên trường sắp xếp
     * @param sort Chiều sắp xếp (asc, desc)
     * @return PageResponse<List<SubjectRegistrationForMenteeResponse>>
     */
    @GetMapping("/mentee")
    @PreAuthorize("hasRole('MENTEE')")
    public ResponseEntity<ApiResponse<PageResponse<List<SubjectRegistrationForMenteeResponse>>>> searchRegistrationsByMentee(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort) {

        SearchRequest<SubjectRegistrationForMenteeResponse> searchRequest
                = new SearchRequest<>(search, page, size, name, sort, SubjectRegistrationForMenteeResponse.class);

        PageResponse<List<SubjectRegistrationForMenteeResponse>> result =
                subjectRegistrationService.searchRegistrationsByMentee(searchRequest);

        ApiResponse<PageResponse<List<SubjectRegistrationForMenteeResponse>>> response = new ApiResponse<>(
                ResponseCode.SUCCESS,
                result
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
