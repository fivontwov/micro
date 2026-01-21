package com.dpp.ddp_study_management.repository;

import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForAdminResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMenteeResponse;
import com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMentorResponse;
import com.dpp.ddp_study_management.model.MentorMenteeRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentorMenteeRegistrationRepository extends JpaRepository<MentorMenteeRegistration, Long> {
    boolean existsByMenteeId(Long menteeId);
    boolean existsByMentorId(Long mentorId);

    @Query(value = "SELECT * FROM mentor_mentee_registrations mmr WHERE mmr.mentee_id = ?1 and mmr.mentor_id = ?2", nativeQuery = true)
    MentorMenteeRegistration findByMenteeIdAndMentorId(Long menteeId, Long mentorId);

    @Query("SELECT new com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForAdminResponse(mmr.id, mmr.registerDate, mmr.startDate, mmr.endDate, mmr.mentee.id, mmr.mentee.username, mmr.mentee.name, mmr.mentor.id, mmr.mentor.username, mmr.mentor.name) " +
            "FROM MentorMenteeRegistration mmr " +
            "WHERE (:search IS NULL OR :search = '' OR " +
            "  LOWER(mmr.mentor.username) LIKE %:search% OR " +
            "  LOWER(mmr.mentor.name) LIKE %:search% OR " +
            "  LOWER(mmr.mentee.username) LIKE %:search% OR " +
            "  LOWER(mmr.mentee.name) LIKE %:search%) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'asc' THEN mmr.id END ASC, " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'desc' THEN mmr.id END DESC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'asc' THEN mmr.registerDate END ASC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'desc' THEN mmr.registerDate END DESC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'asc' THEN mmr.startDate END ASC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'desc' THEN mmr.startDate END DESC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'asc' THEN mmr.endDate END ASC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'desc' THEN mmr.endDate END DESC, " +
            "CASE WHEN :sortBy = 'menteeId' AND :sortDirection = 'asc' THEN mmr.mentee.id END ASC, " +
            "CASE WHEN :sortBy = 'menteeId' AND :sortDirection = 'desc' THEN mmr.mentee.id END DESC, " +
            "CASE WHEN :sortBy = 'menteeUsername' AND :sortDirection = 'asc' THEN mmr.mentee.username END ASC, " +
            "CASE WHEN :sortBy = 'menteeUsername' AND :sortDirection = 'desc' THEN mmr.mentee.username END DESC, " +
            "CASE WHEN :sortBy = 'menteeName' AND :sortDirection = 'asc' THEN mmr.mentee.name END ASC, " +
            "CASE WHEN :sortBy = 'menteeName' AND :sortDirection = 'desc' THEN mmr.mentee.name END DESC, " +
            "CASE WHEN :sortBy = 'mentorId' AND :sortDirection = 'asc' THEN mmr.mentor.id END ASC, " +
            "CASE WHEN :sortBy = 'mentorId' AND :sortDirection = 'desc' THEN mmr.mentor.id END DESC, " +
            "CASE WHEN :sortBy = 'mentorUsername' AND :sortDirection = 'asc' THEN mmr.mentor.username END ASC, " +
            "CASE WHEN :sortBy = 'mentorUsername' AND :sortDirection = 'desc' THEN mmr.mentor.username END DESC, " +
            "CASE WHEN :sortBy = 'mentorName' AND :sortDirection = 'asc' THEN mmr.mentor.name END ASC, " +
            "CASE WHEN :sortBy = 'mentorName' AND :sortDirection = 'desc' THEN mmr.mentor.name END DESC")
    List<MentorMenteeRegistrationForAdminResponse> searchForAdmin(@Param("search") String search,
                                                                  @Param("sortBy") String sortBy,
                                                                  @Param("sortDirection") String sortDirection);

    @Query("SELECT new com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMenteeResponse(mmr.id, mmr.registerDate, mmr.startDate, mmr.endDate, mmr.mentor.id, mmr.mentor.username, mmr.mentor.name) " +
            "FROM MentorMenteeRegistration mmr " +
            "WHERE (:search IS NULL OR :search = '' OR " +
            "  LOWER(mmr.mentor.username) LIKE %:search% OR " +
            "  LOWER(mmr.mentor.name) LIKE %:search%) " +
            "AND mmr.mentee.id = :menteeId " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'asc' THEN mmr.id END ASC, " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'desc' THEN mmr.id END DESC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'asc' THEN mmr.registerDate END ASC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'desc' THEN mmr.registerDate END DESC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'asc' THEN mmr.startDate END ASC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'desc' THEN mmr.startDate END DESC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'asc' THEN mmr.endDate END ASC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'desc' THEN mmr.endDate END DESC, " +
            "CASE WHEN :sortBy = 'mentorId' AND :sortDirection = 'asc' THEN mmr.mentor.id END ASC, " +
            "CASE WHEN :sortBy = 'mentorId' AND :sortDirection = 'desc' THEN mmr.mentor.id END DESC, " +
            "CASE WHEN :sortBy = 'mentorUsername' AND :sortDirection = 'asc' THEN mmr.mentor.username END ASC, " +
            "CASE WHEN :sortBy = 'mentorUsername' AND :sortDirection = 'desc' THEN mmr.mentor.username END DESC, " +
            "CASE WHEN :sortBy = 'mentorName' AND :sortDirection = 'asc' THEN mmr.mentor.name END ASC, " +
            "CASE WHEN :sortBy = 'mentorName' AND :sortDirection = 'desc' THEN mmr.mentor.name END DESC")
    List<MentorMenteeRegistrationForMenteeResponse> searchForMentee(@Param("search") String search,
                                                                    @Param("menteeId") Long menteeId,
                                                                    @Param("sortBy") String sortBy,
                                                                    @Param("sortDirection") String sortDirection);

    @Query("SELECT new com.dpp.ddp_study_management.dto.response.registration.MentorMenteeRegistrationForMentorResponse(mmr.id, mmr.registerDate, mmr.startDate, mmr.endDate, mmr.mentee.id, mmr.mentee.username, mmr.mentee.name) " +
            "FROM MentorMenteeRegistration mmr " +
            "WHERE (:search IS NULL OR :search = '' OR " +
            "  LOWER(mmr.mentee.username) LIKE %:search% OR " +
            "  LOWER(mmr.mentee.name) LIKE %:search%) " +
            "AND mmr.mentor.id = :mentorId " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'asc' THEN mmr.id END ASC, " +
            "CASE WHEN :sortBy = 'id' AND :sortDirection = 'desc' THEN mmr.id END DESC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'asc' THEN mmr.registerDate END ASC, " +
            "CASE WHEN :sortBy = 'registerDate' AND :sortDirection = 'desc' THEN mmr.registerDate END DESC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'asc' THEN mmr.startDate END ASC, " +
            "CASE WHEN :sortBy = 'startDate' AND :sortDirection = 'desc' THEN mmr.startDate END DESC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'asc' THEN mmr.endDate END ASC, " +
            "CASE WHEN :sortBy = 'endDate' AND :sortDirection = 'desc' THEN mmr.endDate END DESC, " +
            "CASE WHEN :sortBy = 'menteeId' AND :sortDirection = 'asc' THEN mmr.mentee.id END ASC, " +
            "CASE WHEN :sortBy = 'menteeId' AND :sortDirection = 'desc' THEN mmr.mentee.id END DESC, " +
            "CASE WHEN :sortBy = 'menteeUsername' AND :sortDirection = 'asc' THEN mmr.mentee.username END ASC, " +
            "CASE WHEN :sortBy = 'menteeUsername' AND :sortDirection = 'desc' THEN mmr.mentee.username END DESC, " +
            "CASE WHEN :sortBy = 'menteeName' AND :sortDirection = 'asc' THEN mmr.mentee.name END ASC, " +
            "CASE WHEN :sortBy = 'menteeName' AND :sortDirection = 'desc' THEN mmr.mentee.name END DESC")
    List<MentorMenteeRegistrationForMentorResponse> searchForMentor(@Param("search") String search,
                                                                    @Param("mentorId") Long mentorId,
                                                                    @Param("sortBy") String sortBy,
                                                                    @Param("sortDirection") String sortDirection);
}