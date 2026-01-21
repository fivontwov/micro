package com.dpp.ddp_study_management.service.impl;

import com.dpp.ddp_study_management.model.UserDetailsImpl;
import com.dpp.ddp_study_management.model.User;
import com.dpp.ddp_study_management.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findByUsername(username);

        return new UserDetailsImpl(
                user.getUsername(),
                user.getPassword(),
                user.getAuthorities()
        );
    }
}