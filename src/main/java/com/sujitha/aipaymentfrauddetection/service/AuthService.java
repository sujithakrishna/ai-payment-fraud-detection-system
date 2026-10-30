package com.sujitha.aipaymentfrauddetection.service;

import com.sujitha.aipaymentfrauddetection.dto.LoginRequest;
import com.sujitha.aipaymentfrauddetection.dto.LoginResponse;
import com.sujitha.aipaymentfrauddetection.dto.RegisterRequest;
import com.sujitha.aipaymentfrauddetection.dto.RegisterResponse;
import com.sujitha.aipaymentfrauddetection.entity.User;
import com.sujitha.aipaymentfrauddetection.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // ---------------------------------------------------------
    // REGISTER
    // ---------------------------------------------------------

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        // -----------------------------------------------------
        // Check duplicate email
        // -----------------------------------------------------

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        if (userRepository.existsByEmail(email)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered");
        }

        // -----------------------------------------------------
        // Check duplicate phone
        // -----------------------------------------------------

        if (request.getPhone() != null
                && !request.getPhone().isBlank()
                && userRepository.existsByPhone(
                request.getPhone().trim())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phone number is already registered");
        }

        // -----------------------------------------------------
        // Create user
        // -----------------------------------------------------

        User user = new User();

        user.setFullName(
                request.getFullName().trim());

        user.setEmail(email);

        user.setPhone(
                request.getPhone() == null
                        ? null
                        : request.getPhone().trim());

        // -----------------------------------------------------
        // BCrypt password hashing
        // -----------------------------------------------------

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()));

        // -----------------------------------------------------
        // Default account values
        // -----------------------------------------------------

        user.setAccountStatus("ACTIVE");

        user.setRole("USER");

        user.setCreatedAt(LocalDateTime.now());

        // -----------------------------------------------------
        // Save user
        // -----------------------------------------------------

        user = userRepository.save(user);

        // -----------------------------------------------------
        // Build response
        // -----------------------------------------------------

        RegisterResponse response =
                new RegisterResponse();

        response.setUserId(user.getUserId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setAccountStatus(
                user.getAccountStatus());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());

        return response;
    }

    // ---------------------------------------------------------
    // LOGIN
    // ---------------------------------------------------------

    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase();

        // -----------------------------------------------------
        // Find user
        // -----------------------------------------------------

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid email or password"));

        // -----------------------------------------------------
        // Check account status
        // -----------------------------------------------------

        if (!"ACTIVE".equalsIgnoreCase(
                user.getAccountStatus())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "User account is not active");
        }

        // -----------------------------------------------------
        // Verify BCrypt password
        // -----------------------------------------------------

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password");
        }

        // -----------------------------------------------------
        // Build login response
        // -----------------------------------------------------

        LoginResponse response =
                new LoginResponse();

        response.setUserId(user.getUserId());
        response.setFullName(user.getFullName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setAccountStatus(
                user.getAccountStatus());
        response.setMessage("Login successful");

        // -----------------------------------------------------
        // Generate JWT
        // -----------------------------------------------------

        response.setToken(
                jwtService.generateToken(user));

        return response;
    }
}
