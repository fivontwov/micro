package com.dpp.ddp_study_management.dto.response.subject;

import com.dpp.ddp_study_management.dto.response.user.InvalidMentorInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Setter
@Getter
public class SubjectCreationResponse {
    List<InvalidMentorInfo> invalidMentors;
}