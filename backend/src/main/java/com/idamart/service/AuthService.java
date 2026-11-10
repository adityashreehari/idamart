package com.idamart.service;

import com.idamart.dto.AuthResponse;
import com.idamart.dto.LoginRequest;
import com.idamart.dto.RegisterRequest;
import com.idamart.dto.UserResponse;
import com.idamart.security.AppUserPrincipal;

public interface AuthService {

    /** Creates a CUSTOMER account and logs them in straight away. */
    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse me(AppUserPrincipal principal);
}
