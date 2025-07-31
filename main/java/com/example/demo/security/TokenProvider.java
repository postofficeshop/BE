package com.example.demo.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserEntity;
import com.example.demo.security.vo.CustomUser;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class TokenProvider {

	private static final String SECRET_KEY = "FlRpX30pMqDbiAkmlfArbrmVkDD4RqISskGZmBFax5oGVxzXXWUzTR5JyskiHMIV9M1Oicegkpi46AdvrcX1E6CmTUBc6IFbTPiD";

	private static final Key SIGNING_KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
	
	public String create(UserEntity userEntity) {
		Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

		return Jwts.builder()
			.signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
			.setSubject(String.valueOf(userEntity.getId()))
			.setIssuer("demo app")
			.setIssuedAt(new Date())
			.setExpiration(expiryDate)
			.compact();
	}

	public String validateAndGetUserId(String token) {
		Claims claims = Jwts.parserBuilder()
            .setSigningKey(SIGNING_KEY)
            .build()
            .parseClaimsJws(token)
            .getBody();

		return claims.getSubject();
	}

	public String create(final Authentication authentication) {
		CustomUser userPrincipal = (CustomUser) authentication.getPrincipal();

		Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

		return Jwts.builder()
			.setSubject(userPrincipal.getName())
			.setIssuedAt(new Date())
			.setExpiration(expiryDate)
			.signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
			.compact();
	}

	public String createByUserId(final Long userId) {
		Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

		return Jwts.builder()
			.setSubject(String.valueOf(userId))
			.setIssuedAt(new Date())
			.setExpiration(expiryDate)
			.signWith(SIGNING_KEY, SignatureAlgorithm.HS512)
			.compact();
	}
}
