package com.flightapp.demo.util;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
	@Value("${jwt.secret}")
	private String secret;
	@Value("${security.jwt.expiration-minutes}")
	private long expirationMinutes;

	public String generateToken(String username, String roles) {
		Instant now = Instant.now();
		return Jwts.builder().setSubject(username).setIssuedAt(Date.from(now))
				.setExpiration(Date.from(now.plus(Duration.ofMinutes(expirationMinutes)))).claim("roles", roles)
				.signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
				.compact();
	}

	public boolean validateToken(String token) {
		try {
			Claims claims = Jwts.parserBuilder()
					.setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).build()
					.parseClaimsJws(token).getBody();

			return claims.getExpiration() != null && claims.getExpiration().after(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	public String extractUsername(String token) {
		return Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8))).build()
				.parseClaimsJws(token).getBody().getSubject();
	}

	public List<GrantedAuthority> getAuthorities(String token) {
		Claims claims = Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
				.build().parseClaimsJws(token).getBody();
		String role = claims.get("roles", String.class);
		if (role != null) {
			return List.of(new SimpleGrantedAuthority("ROLE_" + role));
		}
		return List.of(new SimpleGrantedAuthority("ROLE_USER"));
	}

}
