package com.bokl.homerental.security;

import com.bokl.homerental.service.AppConfigService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private final JwtProperties    jwtProperties;
    private final AppConfigService appConfigService;

    public JwtTokenProvider(JwtProperties jwtProperties, AppConfigService appConfigService) {
        this.jwtProperties    = jwtProperties;
        this.appConfigService = appConfigService;
    }

    // ── Token generation ─────────────────────────────────────────────────────

    /**
     * Generates a signed JWT access token.
     * Expiry is read from {@code app_config} at generation time so it can be
     * changed in the database without restarting the service.
     */
    public String generateAccessToken(Long userId, String username,
                                      Collection<? extends GrantedAuthority> authorities) {
        int expiryMinutes = appConfigService.getInt("jwt_access_expiry_minutes", 60);
        Instant now = Instant.now();
        List<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiryMinutes, ChronoUnit.MINUTES)))
                .signWith(secretKey())
                .compact();
    }

    public long accessTokenExpirySeconds() {
        return appConfigService.getInt("jwt_access_expiry_minutes", 60) * 60L;
    }

    // ── Token validation ─────────────────────────────────────────────────────

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // ── Claim extraction ─────────────────────────────────────────────────────

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public Instant getIssuedAt(String token) {
        return parseClaims(token).getIssuedAt().toInstant();
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        Object raw = parseClaims(token).get("roles");
        if (raw instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        return List.of();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey secretKey() {
        byte[] keyBytes = Base64.getUrlDecoder().decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
