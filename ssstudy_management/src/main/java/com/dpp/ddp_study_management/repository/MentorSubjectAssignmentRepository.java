package com.dpp.ddp_study_management.repository;

import com.dpp.ddp_study_management.model.MentorSubjectAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorSubjectAssignmentRepository extends JpaRepository<MentorSubjectAssignment, Long> {
    List<MentorSubjectAssignment> findBySubjectId(Long subjectId);
    void deleteBySubjectIdAndMentorId(Long subjectId, Long MentorId);
    boolean existsByMentorId(Long mentorId);

    @Query("""
            SELECT msa
            FROM MentorSubjectAssignment msa
            JOIN msa.mentor m
            JOIN msa.subject s
            WHERE m.id = :mentorId
            AND (:search IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(s.description) LIKE LOWER(CONCAT('%', :search, '%')))
    """)
    Page<MentorSubjectAssignment> findByMentorIdWithSearch(@Param("mentorId") Long mentorId, @Param("search") String search, Pageable pageable);
}