package com.dpp.ddp_study_management.repository;

import com.dpp.ddp_study_management.dto.response.subject.AssignedSubject;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForMenteeResponse;
import com.dpp.ddp_study_management.model.SubjectRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubjectRegistrationRepository extends JpaRepository<SubjectRegistration, Long> {
    boolean existsByMenteeId(Long menteeId);

    @Query("""
            SELECT new com.dpp.ddp_study_management.dto.response.subject.AssignedSubject(
                s.id,
                s.name
            )
            FROM SubjectRegistration sr
            JOIN sr.mentee m
            JOIN sr.subject s
            WHERE m.id = :menteeId
    """)
    List<AssignedSubject> findAssignedSubjectsByMenteeId(@Param("menteeId") Long menteeId);

    @Query(value = "SELECT * FROM subject_registrations sr WHERE sr.mentee_id = ?1 and sr.subject_id = ?2", nativeQuery = true)
    SubjectRegistration findByMenteeIdAndSubjectId(Long menteeId, Long subjectId);

    boolean existsBySubjectId(Long subjectId);

    @Query("SELECT new com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForAdminResponse(sr.id, sr.registerDate, sr.startDate, sr.endDate, sr.subject.id, sr.subject.name, sr.subject.description, sr.mentee.id, sr.mentee.username, sr.mentee.name) " +
            "FROM SubjectRegistration sr " +
            "WHERE (:search IS NULL OR :search = '' OR " +
            "  LOWER(sr.subject.name) LIKE %:search% OR " +
            "  LOWER(sr.subject.description) LIKE %:search% OR " +
            "  LOWER(sr.mentee.username) LIKE %:search% OR " +
            "  LOWER(sr.mentee.name) LIKE %:search%) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'asc' THEN sr.id END ASC, " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'desc' THEN sr.id END DESC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'asc' THEN sr.registerDate END ASC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'desc' THEN sr.registerDate END DESC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'asc' THEN sr.startDate END ASC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'desc' THEN sr.startDate END DESC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'asc' THEN sr.endDate END ASC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'desc' THEN sr.endDate END DESC, " +
            "CASE WHEN :sortBy = 'subjectId' AND :sortDirection = 'asc' THEN sr.subject.id END ASC, " +
            "CASE WHEN :sortBy = 'subjectId' AND :sortDirection = 'desc' THEN sr.subject.id END DESC, " +
            "CASE WHEN :sortBy = 'subjectName' AND :sortDirection = 'asc' THEN sr.subject.name END ASC, " +
            "CASE WHEN :sortBy = 'subjectName' AND :sortDirection = 'desc' THEN sr.subject.name END DESC, " +
            "CASE WHEN :sortBy = 'subjectDescription' AND :sortDirection = 'asc' THEN sr.subject.description END ASC, " +
            "CASE WHEN :sortBy = 'subjectDescription' AND :sortDirection = 'desc' THEN sr.subject.description END DESC, " +
            "CASE WHEN :sortBy = 'menteeId' AND :sortDirection = 'asc' THEN sr.mentee.id END ASC, " +
            "CASE WHEN :sortBy = 'menteeId' AND :sortDirection = 'desc' THEN sr.mentee.id END DESC, " +
            "CASE WHEN :sortBy = 'menteeUsername' AND :sortDirection = 'asc' THEN sr.mentee.username END ASC, " +
            "CASE WHEN :sortBy = 'menteeUsername' AND :sortDirection = 'desc' THEN sr.mentee.username END DESC, " +
            "CASE WHEN :sortBy = 'menteeName' AND :sortDirection = 'asc' THEN sr.mentee.name END ASC, " +
            "CASE WHEN :sortBy = 'menteeName' AND :sortDirection = 'desc' THEN sr.mentee.name END DESC")
    List<SubjectRegistrationForAdminResponse> searchForAdmin(@Param("search") String search,
                                                             @Param("sortBy") String sortBy,
                                                             @Param("sortDirection") String sortDirection);

    @Query("SELECT new com.dpp.ddp_study_management.dto.response.registration.SubjectRegistrationForMenteeResponse(sr.id, sr.registerDate, sr.startDate, sr.endDate, sr.subject.id, sr.subject.name, sr.subject.description) " +
            "FROM SubjectRegistration sr " +
            "WHERE (:search IS NULL OR :search = '' OR " +
            "  LOWER(sr.subject.name) LIKE %:search% OR " +
            "  LOWER(sr.subject.description) LIKE %:search%) " +
            "AND sr.mentee.id = :menteeId " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'asc' THEN sr.id END ASC, " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'desc' THEN sr.id END DESC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'asc' THEN sr.registerDate END ASC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'desc' THEN sr.registerDate END DESC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'asc' THEN sr.startDate END ASC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'desc' THEN sr.startDate END DESC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'asc' THEN sr.endDate END ASC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'desc' THEN sr.endDate END DESC, " +
            "CASE WHEN :sortBy = 'subjectId' AND :sortDirection = 'asc' THEN sr.subject.id END ASC, " +
            "CASE WHEN :sortBy = 'subjectId' AND :sortDirection = 'desc' THEN sr.subject.id END DESC, " +
            "CASE WHEN :sortBy = 'subjectName' AND :sortDirection = 'asc' THEN sr.subject.name END ASC, " +
            "CASE WHEN :sortBy = 'subjectName' AND :sortDirection = 'desc' THEN sr.subject.name END DESC, " +
            "CASE WHEN :sortBy = 'subjectDescription' AND :sortDirection = 'asc' THEN sr.subject.description END ASC, " +
            "CASE WHEN :sortBy = 'subjectDescription' AND :sortDirection = 'desc' THEN sr.subject.description END DESC")
    List<SubjectRegistrationForMenteeResponse> searchForMentee(@Param("search") String search,
                                                               @Param("menteeId") Long menteeId,
                                                               @Param("sortBy") String sortBy,
                                                               @Param("sortDirection") String sortDirection);
}