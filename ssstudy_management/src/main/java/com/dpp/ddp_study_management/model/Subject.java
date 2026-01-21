package com.dpp.ddp_study_management.model;

import com.dpp.ddp_study_management.common.validation.annotation.MaxWords;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "subjects")
@Getter
@Setter
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)

    @MaxWords(max=20)
    private String name;

    @MaxWords(max=100)
    private String description;

    @OneToMany(mappedBy = "subject")
    private List<MentorSubjectAssignment> mentorSubjectAssignedList;

    @OneToMany(mappedBy = "subject")
    private List<SubjectRegistration> subjectRegistrationList;

}