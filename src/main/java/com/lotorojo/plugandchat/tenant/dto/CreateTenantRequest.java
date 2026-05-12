package com.lotorojo.plugandchat.tenant.dto;

public record CreateTenantRequest(String name, String adminEmail, String adminPassword) {
}
