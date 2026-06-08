package com.example.bai1.security.jwt;

import com.example.bai1.model.entity.Token;
import com.example.bai1.model.entity.Employee;
import com.example.bai1.repository.TokenRepository;
import com.example.bai1.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JWTProvider {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    @Value("${jwt.secret}")
    private String secret;
    @Value("${jwt.expiration}")
    private long expiration;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        String token = Jwts.builder().subject(username).issuedAt(now).expiration(expiryDate).signWith(getSecretKey()).compact();
        Employee user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found"));

        Token accessToken = Token.builder()
                .token(token)
                .type("access")
                .user(user)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(accessToken);

        return token;
    }

    public Boolean validateToken(String token) {
        try {
            Token accessToken = tokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Token not found"));
            if (accessToken.getExpired() || accessToken.getRevoked()) {
                return false;
            }
            Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUsernameByToken(String token) {
        return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token).getPayload().getSubject();
    }
}
