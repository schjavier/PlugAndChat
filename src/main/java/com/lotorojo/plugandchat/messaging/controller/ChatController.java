package com.lotorojo.plugandchat.messaging.controller;

import com.lotorojo.plugandchat.messaging.dto.ChatMessageRequest;
import com.lotorojo.plugandchat.messaging.dto.ChatMessageResponse;
import com.lotorojo.plugandchat.messaging.entity.Message;
import com.lotorojo.plugandchat.messaging.mapper.MessageMapper;
import com.lotorojo.plugandchat.messaging.service.MessageService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final MessageService messageService;
    private final MessageMapper messageMapper;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate,  MessageService messageService, MessageMapper messageMapper) {
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.messageService = messageService;
        this.messageMapper = messageMapper;
    }

    @MessageMapping("/tenants/{tenantId}/rooms/{roomId}/send")
    public void broadcast(@DestinationVariable String tenantId,
                          @DestinationVariable String roomId,
                          Principal principal,
                          ChatMessageRequest request){

        Message message = messageService.saveMsg(UUID.fromString(roomId), request.content(), principal);
        ChatMessageResponse chatMessageResponse = messageMapper.toDTO(message);

        String destination = "/topic/tenants/" + tenantId + "/rooms/" + roomId;

        simpMessagingTemplate.convertAndSend(destination, chatMessageResponse);

    }


}
