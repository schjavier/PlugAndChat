package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.messaging.dto.AssignAgentRequest;
import com.lotorojo.plugandchat.messaging.dto.CreateRoomRequest;
import com.lotorojo.plugandchat.messaging.entity.Room;

import java.util.UUID;

public interface RoomService {

    Room createRoom(CreateRoomRequest createRoomRequest);
    Room assignAgentToRoom(AssignAgentRequest assignAgentRequest);
    Room closeRoom(UUID roomId);
    Room getRoomById(UUID roomId);
}
