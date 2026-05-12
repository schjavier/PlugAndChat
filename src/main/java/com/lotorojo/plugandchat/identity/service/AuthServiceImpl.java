package com.lotorojo.plugandchat.identity.service;

import com.lotorojo.plugandchat.core.security.JwtService;
import com.lotorojo.plugandchat.core.tenant.TenantContext;
import com.lotorojo.plugandchat.identity.dto.LoginRequest;
import com.lotorojo.plugandchat.identity.dto.LoginResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Authentication Auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.email(),
                loginRequest.password()));

        UserDetails userDetails = (UserDetails) Auth.getPrincipal();

        String token = jwtService.generateToken(
                userDetails,
                TenantContext.getCurrentTenant());

        return new LoginResponse(token, userDetails.getUsername());

    }
}
