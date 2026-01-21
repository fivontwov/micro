package com.dpp.ddp_study_management.repository;

import com.dpp.ddp_study_management.model.ERole;
import com.dpp.ddp_study_management.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByName(ERole name);

    Optional<Role> findByName(ERole name);
}