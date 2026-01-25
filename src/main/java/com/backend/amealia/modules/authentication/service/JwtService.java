package com.backend.amealia.modules.authentication.service;

import com.backend.amealia.modules.user.dto.RefreshTokenDTO;
import com.backend.amealia.modules.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtService {
    @Value("${app.security.jwt.secret}")
    private String secret;

    @Value("${app.security.jwt.access.token.expiry}")
    private long accessTokenExpiry;

    @Value("${app.security.jwt.refresh.token.expiry}")
    private long refreshTokenExpiry;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUserCode())
                .claim("email", user.getEmail())
                .claim("type", "Access")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiry * 1000))
                .signWith(getSigningKey())
                .compact();
    }

    public RefreshTokenDTO generateRefreshToken(User user) {
        String jti = UUID.randomUUID().toString();

        String refreshToken = Jwts.builder()
                .setSubject(user.getUserCode())
                .setId(jti)
                .claim("type", "Refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpiry * 1000))
                .signWith(getSigningKey())
                .compact();

        return new RefreshTokenDTO(refreshToken, jti);
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
