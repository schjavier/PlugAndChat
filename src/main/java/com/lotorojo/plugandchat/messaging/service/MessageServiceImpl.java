package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Message;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.repository.MessageRepository;
import com.lotorojo.plugandchat.messaging.validations.MessagingValidations;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final RoomService roomService;
    private final GuestService guestService;
    private final AgentService agentService;
    private final MessagingValidations messagingValidations;

    public MessageServiceImpl(MessageRepository messageRepository,
                              RoomService roomService,
                              GuestService guestService,
                              AgentService agentService,
                              MessagingValidations messagingValidations) {
        this.messageRepository = messageRepository;
        this.roomService = roomService;
        this.guestService = guestService;
        this.agentService = agentService;
        this.messagingValidations = messagingValidations;
    }

    @Override
    @Transactional
    public Message saveMsg(UUID roomId, String content, Principal principal) {
        Room room = roomService.getRoomById(roomId);
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;

        messagingValidations.validateRoomIsOpen(room);

        Message message = new Message();
        message.setRoom(room);
        message.setContent(content);
        message.setSendDate(LocalDateTime.now());

        if (isGuest(auth)) {
            String guestEmail = auth.getName();
            Guest guest = guestService.getGuestByEmailAndTenant(guestEmail, room.getTenant().getUuid());

            messagingValidations.validateGuestOwnsRoom(room, guest);

            message.setGuest(guest);

        } else {
            String agentEmail = auth.getName();
            Agent agent = agentService.getByUserAccountEmail(agentEmail);

            messagingValidations.agentTenantMatch(room, agent);

            message.setAgent(agent);

        }

        return messageRepository.save(message);

    }

    @Override
    public List<Message> getMessageByRoom(UUID roomId, Principal principal) {
        Room room = roomService.getRoomById(roomId);
        UsernamePasswordAuthenticationToken auth = (UsernamePasswordAuthenticationToken) principal;

        if (isGuest(auth)) {

            String guestEmail = auth.getName();
            messagingValidations.validateRoomGuestEmailAndGuestMailMatch(room, guestEmail);

        } else {

            String agentEmail = auth.getName();
            Agent agent = agentService.getByUserAccountEmail(agentEmail);
            messagingValidations.agentTenantMatch(room, agent);

        }

        return messageRepository.findByRoomUuidOrderBySendDateAsc(roomId);
    }

    private boolean isGuest(UsernamePasswordAuthenticationToken auth) {
        return auth.getAuthorities()
                .stream()
                .anyMatch(a -> "ROLE_GUEST".equals(a.getAuthority()));

    }

}
