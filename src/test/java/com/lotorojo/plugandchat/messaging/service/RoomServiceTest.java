package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.TestDataFactory;
import com.lotorojo.plugandchat.core.tenant.TenantContext;
import com.lotorojo.plugandchat.messaging.dto.CreateRoomRequest;
import com.lotorojo.plugandchat.messaging.entity.*;
import com.lotorojo.plugandchat.messaging.repository.RoomRepository;
import com.lotorojo.plugandchat.messaging.validations.MessagingValidations;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.service.TenantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    @Mock
    private GuestService guestService;
    @Mock
    private TenantService tenantService;
    @Mock
    private RoomRepository roomRepository;
    @Mock
    private AgentService agentService;
    @Mock
    private MessagingValidations messagingValidations;

    @InjectMocks
    private RoomServiceImpl roomService;

    private Guest guest;
    private Tenant tenant;
    private Agent agent;

    @BeforeEach
    public void setUp(){

        guest = TestDataFactory.defaultGuest();
        agent = TestDataFactory.defaultAgent();
        tenant = TestDataFactory.defaultTenant();

    }

    @Test
    public void shouldCreteRoomCorrectly(){
        TenantContext.setCurrentTenant(UUID.randomUUID());

        tenant = Tenant.builder().uuid(TenantContext.getCurrentTenant()).build();
        guest = Guest.builder().name("guest").email("test@guest.com").build();

        CreateRoomRequest request = new CreateRoomRequest(guest.getName(), guest.getEmail());

        when(tenantService.getTenant(TenantContext.getCurrentTenant())).thenReturn(tenant);
        when(guestService.getOrCreateGuest(request.guestName(), request.guestEmail(), tenant)).thenReturn(guest);

        when(roomRepository.save(any(Room.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Room result = roomService.createRoom(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(RoomStatus.WAITING);
        assertThat(result.getTenant().getUuid()).isEqualTo(TenantContext.getCurrentTenant());
        assertThat(result.getGuest().getEmail()).isEqualTo("test@guest.com");
        assertThat(result.getGuest().getName()).isEqualTo("guest");

        verify(roomRepository).save(any(Room.class));

    }



}
