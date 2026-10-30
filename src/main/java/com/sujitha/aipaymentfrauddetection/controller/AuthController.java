package com.sujitha.aipaymentfrauddetection.controller;

import com.sujitha.aipaymentfrauddetection.dto.LoginRequest;
import com.sujitha.aipaymentfrauddetection.dto.LoginResponse;
import com.sujitha.aipaymentfrauddetection.dto.RegisterRequest;
import com.sujitha.aipaymentfrauddetection.dto.RegisterResponse;
import com.sujitha.aipaymentfrauddetection.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(
            @Valid @RequestBody RegisterRequest request) {

        return authService.register(request);
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    @PostMapping("/login")
    public LoginResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authService.login(request);
    }
}