package com.dpp.ddp_study_management.repository;

import com.dpp.ddp_study_management.model.Subject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    boolean existsById(Long id);
    boolean existsByName(String name);
    @Query("SELECT s FROM Subject s")
    Page<Subject> findAllWithMentors(Pageable pageable);

    @Query("SELECT s FROM Subject s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Subject> findByNameContainingIgnoreCaseWithMentors(@Param("search") String search, Pageable pageable);

    @Query(value="SELECT s FROM Subject s WHERE NOT EXISTS (SELECT sr FROM SubjectRegistration sr WHERE sr.subject.id = s.id AND sr.mentee.id = :menteeId)")
    List<Subject> findUnregisteredSubjectsByMenteeId(@Param("menteeId") Long menteeId);

}