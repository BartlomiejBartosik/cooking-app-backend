package org.example.cookingappbackend.service;

import java.util.Base64;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.example.cookingappbackend.model.User;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final String SECRET_KEY = "6LJsHL0QnYxtSuNipfuD6iHLT9gzTmiGUsVbvc1/O5Y=";
    private final byte[] key = Base64.getDecoder().decode(SECRET_KEY);

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                .signWith(SignatureAlgorithm.HS256, Base64.getDecoder().decode(SECRET_KEY))
                .compact();
    }
    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
    public String extractUsername(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    public String generateRefreshToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
                .signWith(SignatureAlgorithm.HS256, Base64.getDecoder().decode(SECRET_KEY))
                .compact();
    }



    public boolean isRefreshTokenValid(String token, User userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getEmail()) && !isTokenExpired(token);
    }



}
