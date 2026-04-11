package com.smartlibrary.controller;

import com.smartlibrary.dto.auth.AuthenticationRequest;
import com.smartlibrary.dto.auth.AuthenticationResponse;
import com.smartlibrary.dto.auth.RegisterRequest;
import com.smartlibrary.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService service;

    // Ендпоінт для реєстрації: http://localhost:8080/api/v1/auth/register
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @RequestBody RegisterRequest request
    ) {
        return ResponseEntity.ok(service.register(request));
    }

    // Ендпоінт для логіну: http://localhost:8080/api/v1/auth/authenticate
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }
}