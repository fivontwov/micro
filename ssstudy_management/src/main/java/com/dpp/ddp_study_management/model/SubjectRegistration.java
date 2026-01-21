package com.dpp.ddp_study_management.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "subject_registrations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubjectRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "register_date", nullable = false)
    private LocalDateTime registerDate;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "mentee_id")
    private User mentee;

    @ManyToOne
    @JoinColumn(name = "subject_id")
    private Subject subject;
}