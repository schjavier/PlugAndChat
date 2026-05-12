package com.lotorojo.plugandchat.identity.dto;

import java.util.UUID;

public record CreateCredentialDTO(
        UUID UserAccountID,
        String password,
        String provider) {
}
