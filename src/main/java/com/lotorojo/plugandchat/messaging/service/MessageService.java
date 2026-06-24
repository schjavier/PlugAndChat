package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.messaging.entity.Message;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

public interface MessageService {

    Message saveMsg(UUID roomId, String content, Principal principal);
    List<Message> getMessageByRoom(UUID room, Principal principal);
}
