package com.lotorojo.plugandchat.messaging.controller;

import com.lotorojo.plugandchat.messaging.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    public ChatController(SimpMessagingTemplate simpMessagingTemplate) {
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @MessageMapping("/app/tenants/{tenantId}/rooms/{roomId}/send")
    public void broadcast(@DestinationVariable String tenantId,
                          @DestinationVariable String roomId,
                          ChatMessage message){

        String destination = "topic/tenants/" + tenantId + "/rooms/" + roomId;

        //aqui se presistira el mensaje
        simpMessagingTemplate.convertAndSend(destination, message);

    }


}
