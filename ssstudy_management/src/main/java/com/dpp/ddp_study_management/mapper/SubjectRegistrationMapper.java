package com.dpp.ddp_study_management.mapper;

import com.dpp.ddp_study_management.dto.request.registration.SubjectRegistrationCreationRequest;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForMenteeResponse;
import com.dpp.ddp_study_management.model.Subject;
import com.dpp.ddp_study_management.model.SubjectRegistration;
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
public interface SubjectRegistrationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registerDate", source = "request", qualifiedByName = "currentDateTime")
    @Mapping(target = "subject", source = "subject")
    @Mapping(target = "mentee", source = "mentee")
    SubjectRegistration toEntity(SubjectRegistrationCreationRequest request, Subject subject, User mentee);

    @Mapping(target = "id", source = "registration.id")
    @Mapping(target = "registerDate", source = "registration.registerDate")
    @Mapping(target = "startDate", source = "registration.startDate")
    @Mapping(target = "endDate", source = "registration.endDate")
    @Mapping(target = "subjectId", source = "registration.subject.id")
    @Mapping(target = "subjectName", source = "registration.subject.name")
    @Mapping(target = "subjectDescription", source = "registration.subject.description")
    @Mapping(target = "menteeId", source = "registration.mentee.id")
    @Mapping(target = "menteeUsername", source = "registration.mentee.username")
    @Mapping(target = "menteeName", source = "registration.mentee.name")
    SubjectRegistrationForAdminResponse toAdminResponse(SubjectRegistration registration);

    @Mapping(target = "id", source = "registration.id")
    @Mapping(target = "registerDate", source = "registration.registerDate")
    @Mapping(target = "startDate", source = "registration.startDate")
    @Mapping(target = "endDate", source = "registration.endDate")
    @Mapping(target = "subjectId", source = "registration.subject.id")
    @Mapping(target = "subjectName", source = "registration.subject.name")
    @Mapping(target = "subjectDescription", source = "registration.subject.description")
    SubjectRegistrationForMenteeResponse toMenteeResponse(SubjectRegistration registration);

    @Named("currentDateTime")
    static LocalDateTime currentDateTime(SubjectRegistrationCreationRequest request) {
        return LocalDateTime.now();
    }
}