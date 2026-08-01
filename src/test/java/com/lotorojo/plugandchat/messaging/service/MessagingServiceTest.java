package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.messaging.mapper.MessageMapper;
import com.lotorojo.plugandchat.messaging.repository.MessageRepository;
import com.lotorojo.plugandchat.messaging.validations.MessagingValidations;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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


}
