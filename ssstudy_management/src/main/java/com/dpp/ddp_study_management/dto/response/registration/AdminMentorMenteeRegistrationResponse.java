package com.dpp.ddp_study_management.dto.response.registration;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminMentorMenteeRegistrationResponse {
    private long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime registerDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime startDate;

    private Long menteeId;
    private String menteeUsername;
    private String menteeName;

    private Long mentorId;
    private String mentorUsername;
    private String mentorName;
}
