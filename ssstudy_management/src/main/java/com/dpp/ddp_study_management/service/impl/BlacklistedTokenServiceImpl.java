package com.dpp.ddp_study_management.service.impl;

import com.dpp.ddp_study_management.model.BlacklistedToken;
import com.dpp.ddp_study_management.repository.BlacklistedTokenRepository;
import com.dpp.ddp_study_management.service.BlacklistedTokenService;
import com.dpp.ddp_study_management.common.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class BlacklistedTokenServiceImpl implements BlacklistedTokenService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    private final JwtUtil jwtUtil;

    @Override
    public void addBlacklistedToken(String token) {
        Date expirationDate = jwtUtil.parseDate(token);
        LocalDateTime expiresAt = expirationDate.toInstant().minusMillis(59 * 60000)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        BlacklistedToken blacklistedToken = new BlacklistedToken(token, expiresAt);
        blacklistedTokenRepository.save(blacklistedToken);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepository.existsByToken(token);
    }

    // Scheduled task to remove expired token
    @Scheduled(fixedRate = 1200000) // Automatically execute every 20 minutes
    @Transactional
    protected void cleanupExpiredTokens() {
        blacklistedTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
