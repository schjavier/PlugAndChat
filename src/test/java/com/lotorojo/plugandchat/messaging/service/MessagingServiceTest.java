package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.TestDataFactory;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Message;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.mapper.MessageMapper;
import com.lotorojo.plugandchat.messaging.repository.MessageRepository;
import com.lotorojo.plugandchat.messaging.validations.MessagingValidations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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


}
