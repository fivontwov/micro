package com.dpp.ddp_study_management.repository;

import com.dpp.ddp_study_management.model.ERole;
import com.dpp.ddp_study_management.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findById(Long userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long userId);

    Optional<User> findByIdAndRoles_Name(Long id, ERole eRole);

    @Query(value = "SELECT DISTINCT u " +
            "FROM User u " +
            "JOIN u.roles r " +
            "WHERE r.name IN ('MENTOR', 'MENTEE') " +
            "AND (:searchTerm IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
            "OR LOWER(u.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))",
            countQuery = "SELECT COUNT(DISTINCT u.id) " +
                    "FROM User u " +
                    "JOIN u.roles r " +
                    "WHERE r.name IN ('MENTOR', 'MENTEE') " +
                    "AND (:searchTerm IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
                    "OR LOWER(u.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))"
    )
    Page<User> findMentorsOrMenteesWithSearch(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Query(
            value = """
                SELECT DISTINCT u
                FROM User u
                JOIN u.roles r
                WHERE r.name = 'MENTOR'
                AND u.id NOT IN (
                    SELECT msa.mentor.id
                    FROM MentorSubjectAssignment msa
                    WHERE msa.subject.id = :subjectId
                )
                AND (
                    :searchTerm IS NULL
                    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                )
            """,
            countQuery = """
                SELECT COUNT(DISTINCT u.id)
                FROM User u
                JOIN u.roles r
                WHERE r.name = 'MENTOR'
                AND u.id NOT IN (
                    SELECT msa.mentor.id
                    FROM MentorSubjectAssignment msa
                    WHERE msa.subject.id = :subjectId
                )
                AND (
                    :searchTerm IS NULL
                    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                )
            """
    )
    Page<User> findAvailableMentorsForSubjectWithSearch(@Param("subjectId") Long subjectId,
                                                        @Param("searchTerm") String searchTerm,
                                                        Pageable pageable);

    @Query(
            value = """
                SELECT DISTINCT u
                FROM User u
                JOIN u.roles r
                WHERE r.name = 'MENTOR'
                AND u.id NOT IN (
                    SELECT mmr.mentor.id
                    FROM MentorMenteeRegistration mmr
                    WHERE mmr.mentee.id = :menteeId
                )
                AND (
                    :searchTerm IS NULL
                    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                )
            """,
            countQuery = """
                SELECT COUNT(DISTINCT u.id)
                FROM User u
                JOIN u.roles r
                WHERE r.name = 'MENTOR'
                AND u.id NOT IN (
                    SELECT mmr.mentor.id
                    FROM MentorMenteeRegistration mmr
                    WHERE mmr.mentee.id = :menteeId
                )
                AND (
                    :searchTerm IS NULL
                    OR LOWER(u.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                    OR LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%'))
                )
            """
    )
    Page<User> findAvailableMentorsForMenteeWithSearch(@Param("menteeId") Long menteeId,
                                                       @Param("searchTerm") String searchTerm,
                                                       Pageable pageable);
}
