package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.TestDataFactory;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.messaging.entity.*;
import com.lotorojo.plugandchat.messaging.mapper.MessageMapper;
import com.lotorojo.plugandchat.messaging.repository.MessageRepository;
import com.lotorojo.plugandchat.messaging.validations.MessagingValidations;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessagingServiceTest {

    @Mock
    private MessageRepository messageRepository;
    @Mock
    private RoomService roomService;
    @Mock
    private GuestService guestService;
    @Mock
    private AgentService agentService;
    @Mock
    private MessagingValidations  messagingValidations;
    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    private Room room;
    private Guest guest;
    private Agent agent;
    private Message guestMessage;
    private Message agentMessage;

    @BeforeEach
    public void setup() {

        room = TestDataFactory.defaultRoom();
        guest = TestDataFactory.defaultGuest();
        agent = TestDataFactory.defaultAgent();
        guestMessage = TestDataFactory.defaultGuestMessage();
        agentMessage = TestDataFactory.defaultAgentMessage();
    }

    @Test
    public void shouldSaveMsgCorrectlyAsGuest(){
        UUID roomId = UUID.randomUUID();
        when(roomService.getRoomById(roomId)).thenReturn(room);

        UsernamePasswordAuthenticationToken guestPrincipal = new UsernamePasswordAuthenticationToken(
                "guest@prueba.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_GUEST")));

        when(guestService.getGuestByEmailAndTenant(guestPrincipal.getName(), room.getTenant().getUuid()))
                .thenReturn(guest);

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Message result = messageService.saveMsg(roomId, "Hola", guestPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hola");
        assertThat(result.getAgent()).isNull();

        verify(messagingValidations).validateRoomIsOpen(room);
        verify(messagingValidations).validateGuestOwnsRoom(room, guest);

    }

    @Test
    public void shouldSaveMsgCorrectlyAsAgent(){

        UUID roomId = UUID.randomUUID();
        when(roomService.getRoomById(roomId)).thenReturn(room);

        UsernamePasswordAuthenticationToken agentPrincipal = new UsernamePasswordAuthenticationToken(
                "agent@prueba.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_AGENT")));

        when(agentService.getByUserAccountEmail(agentPrincipal.getName())).thenReturn(agent);

        when(messageRepository.save(any(Message.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Message result = messageService.saveMsg(roomId, "Hola", agentPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Hola");
        assertThat(result.getGuest()).isNull();
        assertThat(result.getAgent()).isNotNull();
        verify(messagingValidations).validateRoomIsOpen(room);
        verify(messagingValidations).agentTenantMatch(room, agent);

    }

    @Test
    public void shouldGetMsgByRoomCorrectlyAsGuest(){
        UUID roomId = UUID.randomUUID();
        guest = guest.toBuilder().email("guest@test.com").build();
        room = room.toBuilder().guest(guest).build();

        when(roomService.getRoomById(roomId)).thenReturn(room);

        agentMessage = agentMessage.toBuilder().room(room).content("How Can I help you?").build();
        guestMessage = guestMessage.toBuilder().room(room).content("Hi!").build();

        UsernamePasswordAuthenticationToken guestPrincipal = new UsernamePasswordAuthenticationToken(
                "guest@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_GUEST")));

        when(messageRepository.findByRoomUuidOrderBySendDateAsc(roomId)).thenReturn(List.of(guestMessage, agentMessage));

        List<Message> result = messageService.getMessageByRoom(roomId, guestPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

        assertThat(result.getFirst().getContent()).isEqualTo("Hi!");
        assertThat(result.getFirst().getAgent()).isNull();
        assertThat(result.getFirst().getGuest()).isNotNull();

        assertThat(result.get(1).getContent()).isEqualTo("How Can I help you?");
        assertThat(result.get(1).getAgent()).isNotNull();
        assertThat(result.get(1).getGuest()).isNull();

        verify(messagingValidations).validateRoomGuestEmailAndGuestMailMatch(room, guestPrincipal.getName());

    }

    @Test
    public void shouldGetMsgByRoomCorrectlyAsAgent(){
        UUID roomId = UUID.randomUUID();
        when(roomService.getRoomById(roomId)).thenReturn(room);

        agentMessage = agentMessage.toBuilder().room(room).content("How Can I help you?").build();
        guestMessage = guestMessage.toBuilder().room(room).content("Hi!").build();

        UsernamePasswordAuthenticationToken guestPrincipal = new UsernamePasswordAuthenticationToken(
                "agent@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_AGENT")));

        when(messageRepository.findByRoomUuidOrderBySendDateAsc(roomId)).thenReturn(List.of(guestMessage, agentMessage));
        when(agentService.getByUserAccountEmail(guestPrincipal.getName())).thenReturn(agent);

        List<Message> result = messageService.getMessageByRoom(roomId, guestPrincipal);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);

        assertThat(result.getFirst().getContent()).isEqualTo("Hi!");
        assertThat(result.getFirst().getAgent()).isNull();
        assertThat(result.getFirst().getGuest()).isNotNull();

        assertThat(result.get(1).getContent()).isEqualTo("How Can I help you?");
        assertThat(result.get(1).getAgent()).isNotNull();
        assertThat(result.get(1).getGuest()).isNull();

        verify(messagingValidations).agentTenantMatch(room, agent);

    }

    @Test
    public void shouldNotSaveMsgAndPropagateExceptionWhenRoomIsClosed(){
        UUID roomId = UUID.randomUUID();
        room = room.toBuilder().status(RoomStatus.CLOSED).build();
        when(roomService.getRoomById(roomId)).thenReturn(room);

        doThrow(new IllegalStateException("Room is Already Closed"))
                .when(messagingValidations).validateRoomIsOpen(room);

        UsernamePasswordAuthenticationToken guestPrincipal = new UsernamePasswordAuthenticationToken(
                "guest@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_GUEST")));

        assertThrows(IllegalStateException.class, () ->
                messageService.saveMsg(roomId, "Hola", guestPrincipal));

        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    public void shouldNotSAveMsgAndPropagateExceptionWhenGuestIsNotTheOwner(){
        UUID roomId = UUID.randomUUID();
        when(roomService.getRoomById(roomId)).thenReturn(room);

        UsernamePasswordAuthenticationToken guestPrincipal = new UsernamePasswordAuthenticationToken(
                "guest@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_GUEST")));

        when(guestService.getGuestByEmailAndTenant(guestPrincipal.getName(), room.getTenant().getUuid()))
                .thenReturn(guest);

        doThrow(new BadCredentialsException("guest is not the owner of this room"))
                .when(messagingValidations).validateGuestOwnsRoom(room, guest);

        assertThrows(BadCredentialsException.class, () -> messageService.saveMsg(roomId, "Hola", guestPrincipal));

        verify(messageRepository, never()).save(any(Message.class));

    }

    @Test
    public void shouldNotSaveMsgAndPropagateExceptionWhenAgentTenantMismatch(){
        UUID roomId = UUID.randomUUID();
        when(roomService.getRoomById(roomId)).thenReturn(room);

        UsernamePasswordAuthenticationToken agentPrincipal = new UsernamePasswordAuthenticationToken(
                "agent@prueba.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_AGENT")));

        when(agentService.getByUserAccountEmail(agentPrincipal.getName())).thenReturn(agent);

        doThrow(new BadCredentialsException("Agent and Tenant do not match"))
                .when(messagingValidations).agentTenantMatch(room, agent);

        assertThrows(BadCredentialsException.class, () -> messageService.saveMsg(roomId, "Hola", agentPrincipal));
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    public void shouldNotGetMessagesAndPropagateExceptionWhenGuestEmailMismatch(){
        UUID roomId = UUID.randomUUID();
        guest = guest.toBuilder().email("guest@test.com").build();

        when(roomService.getRoomById(roomId)).thenReturn(room);

        UsernamePasswordAuthenticationToken guestPrincipal = new UsernamePasswordAuthenticationToken(
                "guest@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_GUEST")));

        doThrow(new BadCredentialsException("Room guest email and guest email do not match"))
                .when(messagingValidations).validateRoomGuestEmailAndGuestMailMatch(room, guestPrincipal.getName());

        assertThrows(BadCredentialsException.class, () -> messageService.getMessageByRoom(roomId,  guestPrincipal));

        verify(messageRepository, never()).findByRoomUuidOrderBySendDateAsc(roomId);
    }

    @Test
    public void shouldNotGetMessagesAndPropagateExceptionWhenAgentTenantMismatch(){
        UUID roomId = UUID.randomUUID();

        Tenant tenantA = TestDataFactory.defaultTenant().toBuilder().uuid(UUID.randomUUID()).build();
        room = room.toBuilder().tenant(tenantA).build();

        Tenant tenantB = TestDataFactory.defaultTenant().toBuilder().uuid(UUID.randomUUID()).build();
        UserAccount agentUserAccount = TestDataFactory.defaultUserAccount().toBuilder().tenant(tenantB).build();
        agent = agent.toBuilder().userAccount(agentUserAccount).build();

        when(roomService.getRoomById(roomId)).thenReturn(room);

        UsernamePasswordAuthenticationToken agentPrincipal = new UsernamePasswordAuthenticationToken(
                "agent@test.com",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_AGENT")));

        when(agentService.getByUserAccountEmail(agentPrincipal.getName())).thenReturn(agent);

        doThrow(new BadCredentialsException("Agent and Tenant do not match"))
                .when(messagingValidations).agentTenantMatch(room, agent);

        assertThrows(BadCredentialsException.class, () -> messageService.getMessageByRoom(roomId,  agentPrincipal));
        verify(messageRepository, never()).findByRoomUuidOrderBySendDateAsc(roomId);

    }

}
