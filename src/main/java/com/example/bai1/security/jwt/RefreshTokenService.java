package com.example.bai1.security.jwt;

import com.example.bai1.model.dto.request.RevokeRequest;
import com.example.bai1.model.dto.response.JWTResponse;
import com.example.bai1.model.entity.Token;
import com.example.bai1.model.entity.Employee;
import com.example.bai1.repository.TokenRepository;
import com.example.bai1.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor
public class RefreshTokenService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final JWTProvider jWTProvider;

    public Token generateToken(Employee user) {
        Token token = Token.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .type("refresh")
                .expired(false)
                .revoked(false)
                .build();
        return tokenRepository.save(token);
    }

    public Token findByToken(String token) {
        return tokenRepository.findByToken(token).orElseThrow(() -> new RuntimeException("Token not found"));
    }

    public boolean verifyToken(String token) {
        Token t = findByToken(token);
        return !t.getRevoked() && !t.getExpired();
    }

    public JWTResponse refresh(String token) {
        if (!verifyToken(token)) {
            throw new RuntimeException("Invalid token");
        }

        Token t = findByToken(token);
        String accessTokenStr = jWTProvider.generateToken(t.getUser().getUsername());
        Token accessToken = Token.builder()
                .token(accessTokenStr)
                .user(t.getUser())
                .type("access")
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(accessToken);
        return JWTResponse.builder()
                .accessToken(accessTokenStr)
                .refreshToken(token)
                .build();
    }

    @Transactional
    public void revoke(RevokeRequest request) {
        Token accessToken = tokenRepository.findByToken(request.getAccessToken()).orElseThrow(() -> new RuntimeException("Token not found"));
        accessToken.setRevoked(true);
        accessToken.setExpired(true);
        Token refreshToken = tokenRepository.findByToken(request.getRefreshToken()).orElseThrow(() -> new RuntimeException("Token not found"));
        refreshToken.setRevoked(true);
        refreshToken.setExpired(true);
        tokenRepository.save(accessToken);
        tokenRepository.save(refreshToken);
    }
}
