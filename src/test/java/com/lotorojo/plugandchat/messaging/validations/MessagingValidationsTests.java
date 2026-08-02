package com.lotorojo.plugandchat.messaging.validations;

import com.lotorojo.plugandchat.TestDataFactory;
import com.lotorojo.plugandchat.core.tenant.TenantContext;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.entity.RoomStatus;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
        Guest guestA = TestDataFactory.defaultGuest().toBuilder().uuid(UUID.randomUUID()).build();
        Guest guestB = TestDataFactory.defaultGuest().toBuilder().uuid(UUID.randomUUID()).build();
        Room roomA = TestDataFactory.defaultRoom().toBuilder().guest(guestA).build();

        assertThrows(BadCredentialsException.class, () ->
                messagingValidations.validateGuestOwnsRoom(roomA, guestB));

    }

    @Test
    public void shouldThrowExceptionWhenAgentTenantIsNotRoomTenant() {
        Tenant tenant = TestDataFactory.defaultTenant().toBuilder().uuid(UUID.randomUUID()).build();
        UserAccount userAccount = TestDataFactory.defaultUserAccount().toBuilder().uuid(UUID.randomUUID()).tenant(tenant).build();
        Agent agent = TestDataFactory.defaultAgent().toBuilder().uuid(UUID.randomUUID()).userAccount(userAccount).build();

        Tenant tenantB = TestDataFactory.defaultTenant().toBuilder().uuid(UUID.randomUUID()).build();
        UserAccount userAccountB = TestDataFactory.defaultUserAccount().toBuilder().tenant(tenantB).build();
        Agent agentB = TestDataFactory.defaultAgent().toBuilder().uuid(UUID.randomUUID()).userAccount(userAccountB).build();


        Room roomA = TestDataFactory.defaultRoom().toBuilder().tenant(tenant).agent(agent).build();

        assertThrows(BadCredentialsException.class, () ->
                messagingValidations.agentTenantMatch(roomA, agentB));

    }

    @Test
    public void shouldThrowExceptionWhenRoomIsClosed(){
        Room room = TestDataFactory.defaultRoom().toBuilder().status(RoomStatus.CLOSED).build();

        assertThrows(IllegalStateException.class, () ->
                messagingValidations.validateRoomIsOpen(room));
    }

    @Test
    public void shouldThrowExceptionWhenRoomAndTenantUuidMismatch(){
        Tenant tenant = TestDataFactory.defaultTenant().toBuilder().uuid(UUID.randomUUID()).build();
        TenantContext.setCurrentTenant(tenant.getUuid());

        Tenant tenantB = TestDataFactory.defaultTenant().toBuilder().uuid(UUID.randomUUID()).build();

        Room room = TestDataFactory.defaultRoom().toBuilder().tenant(tenantB).build();

        assertThrows(BadCredentialsException.class, () ->
            messagingValidations.validateTenantAccess(room));

    }

    @Test
    public void shouldThrowExceptionWhenRoomGuestEmailAndGuestEmailMismatch(){

        Room room = TestDataFactory.defaultRoom().toBuilder().uuid(UUID.randomUUID()).build();
        Guest guest = TestDataFactory.defaultGuest().toBuilder().email("mail@guest.com").build();

        assertThrows(BadCredentialsException.class, () ->
                messagingValidations.validateRoomGuestEmailAndGuestMailMatch(room, guest.getEmail()));

    }

}
