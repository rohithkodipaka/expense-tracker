package com.rohithk.expensetracker.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Set;

@Service
public class JwtService {

    private final Key key;
    private final long expireMinutes;

    public JwtService(@Value("${app.jwt.properties.secret}") String secret,
                      @Value("${app.jwt.properties.exp-minutes}") long expireMinutes){
            this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
            this.expireMinutes = expireMinutes;
    }

    public String generateJwtToken(String email, Set<String> roles){
        Instant currentTime = Instant.now();
        Instant expirationTime = currentTime.plusSeconds(expireMinutes*60);
        return Jwts.builder()
                .setSubject(email)
                .claim("roles",roles)
                .setIssuedAt(Date.from(currentTime))
                .setExpiration(Date.from(expirationTime))
                .signWith(key,SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims>  parseToken(String token){
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
