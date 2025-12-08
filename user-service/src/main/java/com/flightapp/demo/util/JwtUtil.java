package com.flightapp.demo.util;


import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration-minutes}")
    private long expirationMinutes;

    

    
    public String generateToken(String id, String email, String roles) {
		Instant now = Instant.now();
		if ("USER".equalsIgnoreCase(roles)) {
            roles = "ROLE_USER";
        } else if ("ADMIN".equalsIgnoreCase(roles)) {
            roles = "ROLE_ADMIN";
        }
		return Jwts.builder().setSubject(id).setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plus(Duration.ofMinutes(expirationMinutes)))).claim("email", email)
				.claim("roles", roles)
				.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
				.compact();
	}


//    // Validate token (signature + expiration)
//    public boolean validateToken(String token) {
//        try {
//            Claims claims = parseClaims(token);
//            return claims.getExpiration() != null && claims.getExpiration().after(new Date());
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    // Extract username claim
//    public String extractUsername(String token) {
//        return parseClaims(token).get("username", String.class);
//    }
//
//    // Extract roles claim
//    public String extractRoles(String token) {
//        return parseClaims(token).get("roles", String.class);
//    }
//
//    // Extract userId (subject)
//    public String extractUserId(String token) {
//        return parseClaims(token).getSubject();
//    }
//
//    // Internal helper
//    private Claims parseClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(getSigningKey())
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
}
