package com.lotorojo.plugandchat.tenant.dto;

import java.util.UUID;

public record TenantResponse(UUID uuid, String name, String apiKey){}
