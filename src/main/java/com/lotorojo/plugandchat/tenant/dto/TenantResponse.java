package com.lotorojo.plugandchat.tenant.dto;

import java.util.UUID;

public record TenantResponse(UUID id, String nombre, String apiKey){}
