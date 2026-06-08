package com.example.bai1.service;

import com.example.bai1.model.dto.request.AuthRequest;
import com.example.bai1.model.dto.response.JWTResponse;
import com.example.bai1.model.entity.Role;
import com.example.bai1.model.entity.Token;
import com.example.bai1.model.entity.Employee;
import com.example.bai1.repository.TokenRepository;
import com.example.bai1.repository.UserRepository;
import com.example.bai1.security.jwt.JWTProvider;
import com.example.bai1.security.jwt.RefreshTokenService;
import com.example.bai1.security.principal.UserPrincipal;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JWTProvider jWTProvider;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final TokenRepository tokenRepository;

    public Employee register(AuthRequest authRequest) {
        Employee user = Employee.builder()
                .username(authRequest.getUsername())
                .password(passwordEncoder.encode(authRequest.getPassword()))
                .enabled(true)
                .roles(Set.of(Role.builder().id(1L).build()))
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public JWTResponse login(AuthRequest authRequest) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                authRequest.getUsername(),
                authRequest.getPassword()
        );

        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        String accessToken = jWTProvider.generateToken(userPrincipal.getUsername());
        Token refreshToken = refreshTokenService.generateToken(userPrincipal.getUser());

        return new JWTResponse(
                accessToken,
                refreshToken.getToken()
        );
    }

//    @Transactional
//    public void logout(String username) {
//        Employee user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found"));
//        tokenRepository.logout(user);
//    }
}
