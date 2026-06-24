package com.lotorojo.plugandchat.messaging.dto;

import com.lotorojo.plugandchat.messaging.service.GuestService;

import java.util.UUID;

public record AssignAgentRequest(UUID roomId, UUID agentId) {
}
