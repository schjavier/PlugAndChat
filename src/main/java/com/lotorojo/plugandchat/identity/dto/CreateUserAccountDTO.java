package com.lotorojo.plugandchat.identity.dto;

import com.lotorojo.plugandchat.identity.entity.Role;

import java.util.UUID;

public record CreateUserAccountDTO(UUID tenantId, String email, Role role) {

}
