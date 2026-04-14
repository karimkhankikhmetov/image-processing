package com.karimkhan.image_processing_service.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    private Date extractExpiration(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    public String generateToken(String username){
        SecretKey key = getSigningKey();

        long now = System.currentTimeMillis();
        long expirationTime = expiration;


        return Jwts.builder()
                .subject(username) // Use subject() instead of setSubject()
                .issuedAt(new Date(now)) // Use issuedAt()
                .expiration(new Date(now + expirationTime)) // Use expiration()
                .signWith(key) // Sign with the key
                .compact(); // Build the compact JWT
    }

    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String username = extractUsername(token);
            return username.equals(userDetails.getUsername())
                    && !extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return false;
        }
    }
}