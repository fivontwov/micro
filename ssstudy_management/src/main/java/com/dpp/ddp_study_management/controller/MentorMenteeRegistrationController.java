package com.dpp.ddp_study_management.controller;

import com.dpp.ddp_study_management.common.dto.ApiResponse;
import com.dpp.ddp_study_management.common.dto.PageResponse;
import com.dpp.ddp_study_management.common.dto.SearchRequest;
import com.dpp.ddp_study_management.common.util.ResponseCode;
import com.dpp.ddp_study_management.dto.request.registration.MentorMenteeRegistrationCreationRequest;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMenteeResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMentorResponse;
import com.dpp.ddp_study_management.service.MentorMenteeRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mentor-mentee-registrations")
@RequiredArgsConstructor
@Validated
public class MentorMenteeRegistrationController {

    private final MentorMenteeRegistrationService mentorMenteeRegistrationService;

    /**
     * Tạo mới danh sách đăng ký mentor cho mentee
     *
     * @param request : đối tượng chứa thông tin đăng ký
     * @return ApiResponse chứa danh sách đăng ký đã được tạo
     */
    @PostMapping("")
    @PreAuthorize("hasRole('MENTEE')")
    public ResponseEntity<ApiResponse<MentorMenteeRegistrationForAdminResponse>> createMentorMenteeList(
            @Valid @RequestBody MentorMenteeRegistrationCreationRequest request){

        MentorMenteeRegistrationForAdminResponse result = mentorMenteeRegistrationService.createMMRgs(request);

        ApiResponse<MentorMenteeRegistrationForAdminResponse> response = new ApiResponse<>(
                ResponseCode.MENTOR_MENTEE_REGISTRATION_CREATED,
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
     * @return PageResponse<List<MentorMenteeRegistrationForAdminResponse>>
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<List<MentorMenteeRegistrationForAdminResponse>>>> searchRegistrationsByAdmin(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort) {

        SearchRequest<MentorMenteeRegistrationForAdminResponse> searchRequest
                = new SearchRequest<>(search, page, size, name, sort, MentorMenteeRegistrationForAdminResponse.class);

        PageResponse<List<MentorMenteeRegistrationForAdminResponse>> result =
                mentorMenteeRegistrationService.searchRegistrationsByAdmin(searchRequest);

        ApiResponse<PageResponse<List<MentorMenteeRegistrationForAdminResponse>>> response = new ApiResponse<>(
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
     * @return PageResponse<List<MentorMenteeRegistrationForMenteeResponse>>
     */
    @GetMapping("/mentee")
    @PreAuthorize("hasRole('MENTEE')")
    public ResponseEntity<ApiResponse<PageResponse<List<MentorMenteeRegistrationForMenteeResponse>>>> searchRegistrationsByMentee(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort) {

        SearchRequest<MentorMenteeRegistrationForMenteeResponse> searchRequest
                = new SearchRequest<>(search, page, size, name, sort, MentorMenteeRegistrationForMenteeResponse.class);

        PageResponse<List<MentorMenteeRegistrationForMenteeResponse>> result =
                mentorMenteeRegistrationService.searchRegistrationsByMentee(searchRequest);

        ApiResponse<PageResponse<List<MentorMenteeRegistrationForMenteeResponse>>> response = new ApiResponse<>(
                ResponseCode.SUCCESS,
                result
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Search API cho Mentor
     *
     * @param search Từ khóa tìm kiếm chung
     * @param page Số thứ tự trang (bắt đầu từ 0)
     * @param size Số phần tử trên trang
     * @param name Tên trường sắp xếp
     * @param sort Chiều sắp xếp (asc, desc)
     * @return PageResponse<List<MentorMenteeRegistrationForMentorResponse>>
     */
    @GetMapping("/mentor")
    @PreAuthorize("hasRole('MENTOR')")
    public ResponseEntity<ApiResponse<PageResponse<List<MentorMenteeRegistrationForMentorResponse>>>> searchRegistrationsByMentor(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "id") String name,
            @RequestParam(defaultValue = "asc") String sort) {

        SearchRequest<MentorMenteeRegistrationForMentorResponse> searchRequest
                = new SearchRequest<>(search, page, size, name, sort, MentorMenteeRegistrationForMentorResponse.class);

        PageResponse<List<MentorMenteeRegistrationForMentorResponse>> result =
                mentorMenteeRegistrationService.searchRegistrationsByMentor(searchRequest);

        ApiResponse<PageResponse<List<MentorMenteeRegistrationForMentorResponse>>> response = new ApiResponse<>(
                ResponseCode.SUCCESS,
                result
        );

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}