package com.lotorojo.plugandchat.messaging.dto;

import java.util.UUID;

public record AgentResponse(UUID uuid, String email, String department, String displayName) {
}
