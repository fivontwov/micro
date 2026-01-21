package com.dpp.ddp_study_management.mapper;

import com.dpp.ddp_study_management.dto.response.subject.SubjectResponse;
import com.dpp.ddp_study_management.model.Subject;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface SubjectMapper {
    SubjectResponse toResponse(Subject subject);
}