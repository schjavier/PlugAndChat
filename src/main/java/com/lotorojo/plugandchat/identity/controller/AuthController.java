package com.lotorojo.plugandchat.identity.controller;

import com.lotorojo.plugandchat.core.security.JwtService;
import com.lotorojo.plugandchat.identity.dto.LoginRequest;
import com.lotorojo.plugandchat.identity.dto.LoginResponse;
import com.lotorojo.plugandchat.identity.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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

}
