package com.feedbackflow.service;

import com.feedbackflow.dto.AuthResponse;
import com.feedbackflow.dto.LoginRequest;
import com.feedbackflow.dto.RegisterRequest;
import com.feedbackflow.entity.User;
import com.feedbackflow.exception.BadRequestException;
import com.feedbackflow.repository.UserRepository;
import com.feedbackflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .department(request.getDepartment())
                .build();

        user = userRepository.save(user);
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token).userId(user.getId()).fullName(user.getFullName())
                .email(user.getEmail()).role(user.getRole()).build();
    }

    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException (-> 401) automatically if invalid;
        // handled centrally by GlobalExceptionHandler.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        return AuthResponse.builder()
                .token(token).userId(user.getId()).fullName(user.getFullName())
                .email(user.getEmail()).role(user.getRole()).build();
    }
}
