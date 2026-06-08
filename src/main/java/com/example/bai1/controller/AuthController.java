package com.example.bai1.controller;

import com.example.bai1.model.dto.request.AuthRequest;
import com.example.bai1.model.dto.request.RefreshRequest;
import com.example.bai1.model.dto.request.RevokeRequest;
import com.example.bai1.model.dto.response.JWTResponse;
import com.example.bai1.model.entity.Employee;
import com.example.bai1.security.jwt.RefreshTokenService;
import com.example.bai1.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/register")
    public ResponseEntity<Employee> register(@RequestBody AuthRequest authRequest) {
        return new ResponseEntity<>(authService.register(authRequest), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JWTResponse> login(@RequestBody AuthRequest authRequest) {
        return new ResponseEntity<>(authService.login(authRequest), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JWTResponse> refresh(@RequestBody RefreshRequest request) {
        return new ResponseEntity<>(refreshTokenService.refresh(request.getToken()), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody RevokeRequest request) {
        refreshTokenService.revoke(request);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
