package com.lotorojo.plugandchat.messaging.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ChatMessageResponse(UUID messageId, String senderName, String senderType, String content, LocalDateTime sendDate) {
}
