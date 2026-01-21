package com.dpp.ddp_study_management.service.impl;

import com.dpp.ddp_study_management.common.exception.AppException;
import com.dpp.ddp_study_management.common.exception.ErrorCode;
import com.dpp.ddp_study_management.dto.request.user.RegistrationRequest;
import com.dpp.ddp_study_management.model.ERole;
import com.dpp.ddp_study_management.model.Role;
import com.dpp.ddp_study_management.model.User;
import com.dpp.ddp_study_management.repository.RoleRepository;
import com.dpp.ddp_study_management.repository.UserRepository;
import com.dpp.ddp_study_management.service.RegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    @Override
    @Transactional
    public void register(RegistrationRequest request) {
        // reuse error codes used elsewhere
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }

        // validate and resolve roles
        Set<Role> roleSet = request.getRoles().stream()
                .map(roleName -> {
                    try {
                        ERole eRole = ERole.valueOf(roleName);
                        return roleRepository.findByName(eRole)
                                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
                    } catch (IllegalArgumentException ex) {
                        throw new AppException(ErrorCode.ROLE_NOT_FOUND);
                    }
                })
                .collect(Collectors.toSet());
        // build and save user via helper to avoid modifying existing UserServiceImpl.saveUserFromRequest
        saveUserFromRegistrationRequest(request, roleSet);
    }

    private void saveUserFromRegistrationRequest(RegistrationRequest request, Set<Role> roleSet) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setDescription(request.getDescription());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(roleSet);

        userRepository.save(user);
    }
}
