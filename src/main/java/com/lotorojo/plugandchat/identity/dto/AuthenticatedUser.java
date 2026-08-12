package com.lotorojo.plugandchat.identity.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(String email, List<? extends GrantedAuthority> roles, UUID tenantId) {
}
