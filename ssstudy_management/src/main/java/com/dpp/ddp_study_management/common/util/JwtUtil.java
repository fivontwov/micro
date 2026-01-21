package com.dpp.ddp_study_management.common.util;

import com.dpp.ddp_study_management.model.UserDetailsImpl;
import com.dpp.ddp_study_management.service.impl.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@Component
public class JwtUtil {

    @Value("${SECRET_KEY}")
    private String SECRET_KEY = "";
    private static final long EXPIRATION_TIME = 3_600_000; // 1 hour

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public JwtUtil() {
//        try {
//            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
//            keyGen.init(256);
//            SecretKey sk = keyGen.generateKey();
//            SECRET_KEY = Base64.getEncoder().encodeToString(sk.getEncoded());
//            System.out.println("SECRET_KEY = " + SECRET_KEY); // Using a secret key
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .claim("scp", buildScope(username))
                .signWith(getSecretKey())
                .compact();
    }

    public boolean validateToken(String token, UserDetailsImpl userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String parseToken(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        return null;
    }

    public Date parseDate(String token) {
        return extractAllClaims(token).getExpiration();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    private String buildScope(String username) {
        UserDetailsImpl userDetails = userDetailsService.loadUserByUsername(username);
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet())
                .toString();
    }
}
