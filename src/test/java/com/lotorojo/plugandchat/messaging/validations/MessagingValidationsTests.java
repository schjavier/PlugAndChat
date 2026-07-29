package com.lotorojo.plugandchat.messaging.validations;

import com.lotorojo.plugandchat.TestDataFactory;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessagingValidationsTests {

    private MessagingValidations messagingValidations;

    @BeforeEach
    public void setup() {
        messagingValidations = new MessagingValidations();
    }

    @Test
    public void shouldThrowExceptionWhenGuestIsNotTheOwner() {
        Guest guestA = Guest.builder().uuid(UUID.randomUUID()).build();
        Guest guestB = Guest.builder().uuid(UUID.randomUUID()).build();
        Room roomA = Room.builder().guest(guestA).build();

        assertThrows(BadCredentialsException.class, () ->
                messagingValidations.validateGuestOwnsRoom(roomA, guestB));

    }

    @Test
    public void shouldThrowExceptionWhenAgentTenantIsNotRoomTenant() {
        Tenant tenant = Tenant.builder().uuid(UUID.randomUUID()).build();
        UserAccount userAccount = UserAccount.builder().uuid(UUID.randomUUID()).tenant(tenant).build();
        Agent agent = Agent.builder().uuid(UUID.randomUUID()).userAccount(userAccount).build();

        Tenant tenantB = Tenant.builder().uuid(UUID.randomUUID()).build();
        UserAccount userAccountB = UserAccount.builder().tenant(tenantB).build();
        Agent agentB = Agent.builder().uuid(UUID.randomUUID()).userAccount(userAccountB).build();


        Room roomA = Room.builder().tenant(tenant).agent(agent).build();

        assertThrows(BadCredentialsException.class, () ->
                messagingValidations.agentTenantMatch(roomA, agentB));
        

    }

}
