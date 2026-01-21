package com.dpp.ddp_study_management.dto.response.subject;

import com.dpp.ddp_study_management.dto.response.user.MentorResponse;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@Data
public class SubjectWithMentorsResponse {
    private long id;
    private String name;
    private String description;
    private List<MentorResponse> mentors;
}