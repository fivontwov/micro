package com.dpp.ddp_study_management.mapper;

import com.dpp.ddp_study_management.dto.request.registration.MentorMenteeRegistrationCreationRequest;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMenteeResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMentorResponse;
import com.dpp.ddp_study_management.model.MentorMenteeRegistration;
import com.dpp.ddp_study_management.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDateTime;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface MentorMenteeRegistrationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registerDate", source = "request", qualifiedByName = "currentDateTime")
    @Mapping(target = "mentor", source = "mentor")
    @Mapping(target = "mentee", source = "mentee")
    MentorMenteeRegistration toEntity(MentorMenteeRegistrationCreationRequest request, User mentor, User mentee);

    @Mapping(target = "id", source = "registration.id")
    @Mapping(target = "registerDate", source = "registration.registerDate")
    @Mapping(target = "startDate", source = "registration.startDate")
    @Mapping(target = "endDate", source = "registration.endDate")
    @Mapping(target = "menteeId", source = "registration.mentee.id")
    @Mapping(target = "menteeUsername", source = "registration.mentee.username")
    @Mapping(target = "menteeName", source = "registration.mentee.name")
    @Mapping(target = "mentorId", source = "registration.mentor.id")
    @Mapping(target = "mentorUsername", source = "registration.mentor.username")
    @Mapping(target = "mentorName", source = "registration.mentor.name")
    MentorMenteeRegistrationForAdminResponse toAdminResponse(MentorMenteeRegistration registration);

    @Mapping(target = "id", source = "registration.id")
    @Mapping(target = "registerDate", source = "registration.registerDate")
    @Mapping(target = "startDate", source = "registration.startDate")
    @Mapping(target = "endDate", source = "registration.endDate")
    @Mapping(target = "mentorId", source = "registration.mentor.id")
    @Mapping(target = "mentorUsername", source = "registration.mentor.username")
    @Mapping(target = "mentorName", source = "registration.mentor.name")
    MentorMenteeRegistrationForMenteeResponse toMenteeResponse(MentorMenteeRegistration registration);

    @Mapping(target = "id", source = "registration.id")
    @Mapping(target = "registerDate", source = "registration.registerDate")
    @Mapping(target = "startDate", source = "registration.startDate")
    @Mapping(target = "endDate", source = "registration.endDate")
    @Mapping(target = "menteeId", source = "registration.mentee.id")
    @Mapping(target = "menteeUsername", source = "registration.mentee.username")
    @Mapping(target = "menteeName", source = "registration.mentee.name")
    MentorMenteeRegistrationForMentorResponse toMentorResponse(MentorMenteeRegistration registration);

    @Named("currentDateTime")
    static LocalDateTime currentDateTime(MentorMenteeRegistrationCreationRequest request) {
        return LocalDateTime.now();
    }
}