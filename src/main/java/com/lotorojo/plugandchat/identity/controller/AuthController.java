package com.lotorojo.plugandchat.identity.controller;

import com.lotorojo.plugandchat.core.security.JwtService;
import com.lotorojo.plugandchat.core.tenant.TenantContext;
import com.lotorojo.plugandchat.identity.dto.AuthenticatedUser;
import com.lotorojo.plugandchat.identity.dto.LoginAdminResponse;
import com.lotorojo.plugandchat.identity.dto.LoginRequest;
import com.lotorojo.plugandchat.identity.dto.LoginResponse;
import com.lotorojo.plugandchat.identity.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));

    }

    @PostMapping("/login/admin")
    public ResponseEntity<LoginAdminResponse> loginAdmin(@RequestBody LoginRequest loginRequest,
                                                         HttpServletResponse response) {

        LoginResponse loginResponse = authService.login(loginRequest);

        ResponseCookie cookie = ResponseCookie.from("auth_token", loginResponse.token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(86400)
                .sameSite("lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(new LoginAdminResponse(
                loginResponse.mail(),
                "Authenticated"
        ));

    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("lax")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/auth/me")
    public ResponseEntity<AuthenticatedUser> getAuthenticatedUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID tenantId = TenantContext.getCurrentTenant() != null ? TenantContext.getCurrentTenant() : null;
        return ResponseEntity.ok(new AuthenticatedUser(
                userDetails.getUsername(),
                userDetails.getAuthorities().stream().toList(),
                tenantId
        ));
    }

}
