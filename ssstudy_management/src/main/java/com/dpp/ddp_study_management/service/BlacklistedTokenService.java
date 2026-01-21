package com.dpp.ddp_study_management.service;

public interface BlacklistedTokenService {
    void addBlacklistedToken(String token);
    boolean isTokenBlacklisted(String token);
}