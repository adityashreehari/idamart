package com.idamart.service.impl;

import com.idamart.dto.AuthResponse;
import com.idamart.dto.LoginRequest;
import com.idamart.dto.RegisterRequest;
import com.idamart.dto.UserResponse;
import com.idamart.entity.Role;
import com.idamart.entity.User;
import com.idamart.exception.DuplicateResourceException;
import com.idamart.repository.UserRepository;
import com.idamart.security.AppUserPrincipal;
import com.idamart.security.JwtService;
import com.idamart.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))   // hash, never plain text
                .role(Role.CUSTOMER)                                     // nobody can register as ADMIN
                .build();

        return buildAuthResponse(new AppUserPrincipal(userRepository.save(user)));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        // Throws BadCredentialsException if the email or password is wrong
        // (handled in GlobalExceptionHandler -> 401)
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizeEmail(request.email()), request.password()));

        return buildAuthResponse((AppUserPrincipal) authentication.getPrincipal());
    }

    @Override
    public UserResponse me(AppUserPrincipal principal) {
        return toUserResponse(principal);
    }

    // ---------------- helpers ----------------

    private AuthResponse buildAuthResponse(AppUserPrincipal principal) {
        return new AuthResponse(
                jwtService.generateToken(principal),
                "Bearer",
                jwtService.getExpirationMs(),
                toUserResponse(principal)
        );
    }

    private UserResponse toUserResponse(AppUserPrincipal p) {
        return new UserResponse(p.getId(), p.getFullName(), p.getEmail(), p.getRole().name());
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
