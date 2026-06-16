package com.lotorojo.plugandchat.messaging.repository;

import com.lotorojo.plugandchat.core.exception.NonExistingRoomException;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.entity.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    default Room getRoomOrThrow(UUID roomId) {
        return findById(roomId).orElseThrow(
                ()-> new NonExistingRoomException("La Sala con el id: " + roomId + "no se encuentra"));
    }

    List<Room> findByTenantUuidAndStatus(UUID tenantUuid, RoomStatus roomStatus);
    List<Room> findByAgentUuidAndStatus(UUID agentUuid, RoomStatus roomStatus);

}
