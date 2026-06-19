package com.lotorojo.plugandchat.messaging.dto;

import com.lotorojo.plugandchat.messaging.entity.RoomStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record RoomResponse(
        UUID uuid,
        UUID tenantId,
        UUID guestId,
        String guestName,
        UUID agentId,
        RoomStatus status,
        LocalDateTime createdAt,
        LocalDateTime closedAt,
        String token

) {
}
