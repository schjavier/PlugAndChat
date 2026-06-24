package com.lotorojo.plugandchat.messaging.mapper;

import com.lotorojo.plugandchat.messaging.dto.ChatMessageResponse;
import com.lotorojo.plugandchat.messaging.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {

    public ChatMessageResponse toDTO(Message message) {
        String senderName = message.getAgent() != null
                ? message.getAgent().getDisplayName()
                : message.getGuest().getName();

        String senderType = message.getAgent() != null ? "AGENT" : "GUEST";

        return new ChatMessageResponse(
                message.getUuid(),
                senderName,
                senderType,
                message.getContent(),
                message.getSendDate()
        );
    }

}
