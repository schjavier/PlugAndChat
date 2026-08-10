package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.core.tenant.TenantContext;
import com.lotorojo.plugandchat.messaging.dto.AssignAgentRequest;
import com.lotorojo.plugandchat.messaging.dto.CreateRoomRequest;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.entity.RoomStatus;
import com.lotorojo.plugandchat.messaging.repository.RoomRepository;
import com.lotorojo.plugandchat.messaging.validations.MessagingValidations;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.service.TenantService;
import jakarta.transaction.Transactional;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class RoomServiceImpl implements RoomService {

    private final GuestService guestService;
    private final TenantService tenantService;
    private final RoomRepository roomRepository;
    private final AgentService agentService;
    private final MessagingValidations messagingValidations;

    public RoomServiceImpl(GuestService guestService,
                           TenantService tenantService,
                           RoomRepository roomRepository,
                           AgentService agentService,
                           MessagingValidations messagingValidations) {
        this.guestService = guestService;
        this.tenantService = tenantService;
        this.roomRepository = roomRepository;
        this.agentService = agentService;
        this.messagingValidations = messagingValidations;
    }

    @Override
    @Transactional
    public Room createRoom(CreateRoomRequest createRoomRequest) {

        UUID currentTenantUUID = TenantContext.getCurrentTenant();
        Tenant currentTenant = tenantService.getTenant(currentTenantUUID);

        Optional<Room> activeRoom = roomRepository.getFirstByGuestEmailAndTenantUuidAndStatusNot(
                createRoomRequest.guestEmail(),
                currentTenantUUID,
                RoomStatus.CLOSED
        );

        if (activeRoom.isPresent()) {
            return activeRoom.get();
        }

        Guest guest = guestService.getOrCreateGuest(createRoomRequest.guestName(), createRoomRequest.guestEmail(),  currentTenant);

        Room room = new Room(currentTenant, guest, RoomStatus.WAITING,  LocalDateTime.now());

        return roomRepository.save(room);

    }

    @Override
    @Transactional
    public Room assignAgentToRoom(AssignAgentRequest assignAgentRequest) {
        Agent agent = agentService.getAgent(assignAgentRequest.agentId());
        Room room = roomRepository.getRoomOrThrow(assignAgentRequest.roomId());

        messagingValidations.validateTenantAccess(room);
        messagingValidations.validateRoomIsOpen(room);
        messagingValidations.agentTenantMatch(room, agent);

        room.setStatus(RoomStatus.ACTIVE);
        room.setAgent(agent);

        return roomRepository.save(room);

    }

    @Override
    @Transactional
    public Room closeRoom(UUID roomId) {
        Room room = roomRepository.getRoomOrThrow(roomId);

        messagingValidations.validateRoomIsOpen(room);

        room.setStatus(RoomStatus.CLOSED);
        room.setClosedAt(LocalDateTime.now());
        return roomRepository.save(room);

    }

    @Override
    public Room getRoomById(UUID roomId) {
        return roomRepository.getRoomOrThrow(roomId);
    }
}
