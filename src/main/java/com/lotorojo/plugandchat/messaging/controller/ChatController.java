package com.lotorojo.plugandchat.messaging.controller;

import com.lotorojo.plugandchat.messaging.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage broadcast(ChatMessage message){
        return message;
    }

}
