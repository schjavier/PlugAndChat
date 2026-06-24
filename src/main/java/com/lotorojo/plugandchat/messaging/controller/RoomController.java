package com.lotorojo.plugandchat.messaging.controller;

import com.lotorojo.plugandchat.core.security.JwtService;
import com.lotorojo.plugandchat.messaging.dto.AssignAgentRequest;
import com.lotorojo.plugandchat.messaging.dto.ChatMessageResponse;
import com.lotorojo.plugandchat.messaging.dto.CreateRoomRequest;
import com.lotorojo.plugandchat.messaging.dto.RoomResponse;
import com.lotorojo.plugandchat.messaging.entity.Message;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.mapper.MessageMapper;
import com.lotorojo.plugandchat.messaging.mapper.RoomMapper;
import com.lotorojo.plugandchat.messaging.service.MessageService;
import com.lotorojo.plugandchat.messaging.service.RoomService;
import org.springframework.aop.target.LazyInitTargetSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/rooms")
public class RoomController {

    private final RoomService roomService;
    private final RoomMapper roomMapper;
    private final JwtService  jwtService;
    private final MessageService messageService;
    private final MessageMapper messageMapper;

    public RoomController(RoomService roomService,
                          RoomMapper roomMapper,
                          JwtService jwtService,
                          MessageService messageService,
                          MessageMapper messageMapper) {
        this.roomService = roomService;
        this.roomMapper = roomMapper;
        this.jwtService = jwtService;
        this.messageService = messageService;
        this.messageMapper = messageMapper;
    }

    @PostMapping()
    public ResponseEntity<RoomResponse> createRoom(@RequestBody CreateRoomRequest request){
        Room room = roomService.createRoom(request);
        String token = jwtService.generateGuestToken(room.getGuest().getEmail(), room.getTenant().getUuid());
        return new ResponseEntity<>(roomMapper.toDto(room, token), HttpStatus.CREATED);

    }

    @PostMapping("/assign")
    public ResponseEntity<RoomResponse> assignAgent(@RequestBody AssignAgentRequest request){
        Room room = roomService.assignAgentToRoom(request);
        return ResponseEntity.ok(roomMapper.toDto(room));
    }

    @PostMapping("{roomId}/close")
    public ResponseEntity<RoomResponse> closeRoom(@PathVariable UUID roomId){
        Room room = roomService.closeRoom(roomId);
        return ResponseEntity.ok(roomMapper.toDto(room));
    }

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>>getRoomMessages(@PathVariable UUID roomId, Principal principal){
        List<Message> messages = messageService.getMessageByRoom(roomId, principal);
        List<ChatMessageResponse> chatMessageResponses = messages.stream().map(messageMapper::toDTO).toList();

        return ResponseEntity.ok(chatMessageResponses);
    }

}
