package com.lotorojo.plugandchat.messaging.mapper;

import com.lotorojo.plugandchat.messaging.dto.RoomResponse;
import com.lotorojo.plugandchat.messaging.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public RoomResponse toDto(Room room) {
        return new RoomResponse(
                room.getUuid(),
                room.getTenant().getUuid(),
                room.getGuest().getUuid(),
                room.getGuest().getName(),
                room.getAgent() != null ? room.getAgent().getUuid() : null,
                room.getStatus(),
                room.getCreatedAt(),
                room.getClosedAt()
        );
    }


}
