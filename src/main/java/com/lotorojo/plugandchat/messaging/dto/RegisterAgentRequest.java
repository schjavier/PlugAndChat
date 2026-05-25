package com.lotorojo.plugandchat.messaging.dto;

public record RegisterAgentRequest(String email, String password, String department, String displayName) {
}
