package com.lotorojo.plugandchat.messaging.validations;

import com.lotorojo.plugandchat.core.tenant.TenantContext;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.entity.RoomStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MessagingValidations {

    public void validateGuestOwnsRoom(Room room, Guest guest) {
        if (!room.getGuest().getUuid().equals(guest.getUuid())) {
            throw new BadCredentialsException("Guest is not the owner of the room");
        }
    }

    public void agentTenantMatch(Room room, Agent agent) {
        if (!room.getTenant().getUuid().equals(agent.getUserAccount().getTenant().getUuid())) {
            throw new BadCredentialsException("Agent tenant mismatch");
        }

    }

    public void validateRoomIsOpen(Room room) {
        if (RoomStatus.CLOSED.equals(room.getStatus())) {
            throw new IllegalStateException("Room is already closed");
        }
    }

    public void validateTenantAccess(Room room) {
        UUID currentTenantId = TenantContext.getCurrentTenant();
        if (!room.getTenant().getUuid().equals(currentTenantId)) {
            throw new BadCredentialsException("Access denied: Room does not belong to current tenant");
        }
    }

}
