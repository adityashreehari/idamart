package com.idamart.service.impl;

import com.idamart.dto.AuthResponse;
import com.idamart.dto.LoginRequest;
import com.idamart.dto.RegisterRequest;
import com.idamart.entity.Role;
import com.idamart.entity.User;
import com.idamart.exception.DuplicateResourceException;
import com.idamart.repository.UserRepository;
import com.idamart.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Register: email is lower-cased, password is hashed, role is always CUSTOMER")
    void register_success() {
        when(userRepository.existsByEmail("aditya@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("BCRYPT-HASH");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtService.generateToken(any())).thenReturn("jwt-token");
        when(jwtService.getExpirationMs()).thenReturn(1000L);

        AuthResponse response = authService.register(
                new RegisterRequest(" Aditya ", "  Aditya@Test.com ", "Secret123"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(saved.capture());
        assertThat(saved.getValue().getEmail()).isEqualTo("aditya@test.com");
        assertThat(saved.getValue().getPassword()).isEqualTo("BCRYPT-HASH");   // never the plain password
        assertThat(saved.getValue().getRole()).isEqualTo(Role.CUSTOMER);
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().role()).isEqualTo("CUSTOMER");
    }

    @Test
    @DisplayName("Register with an email that's already used -> 409, nothing saved")
    void register_duplicate() {
        when(userRepository.existsByEmail("taken@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(new RegisterRequest("X", "taken@test.com", "Secret123")))
                .isInstanceOf(DuplicateResourceException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Login with a wrong password is rejected")
    void login_wrongPassword() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("a@test.com", "wrong")))
                .isInstanceOf(BadCredentialsException.class);
        verify(jwtService, never()).generateToken(any());
    }
}
