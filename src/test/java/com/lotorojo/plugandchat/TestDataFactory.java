package com.lotorojo.plugandchat;

import com.lotorojo.plugandchat.identity.entity.AuthProvider;
import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.Role;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.entity.RoomStatus;
import com.lotorojo.plugandchat.tenant.entity.Tenant;

import java.time.LocalDateTime;
import java.util.UUID;

public class TestDataFactory {

    public static Tenant defaultTenant() {
        return Tenant.builder()
                .uuid(UUID.randomUUID())
                .name("TENANT A")
                .api_key("A-secret-api-key")
                .build();
    }

    public static Tenant newTenantRequest() {
        return defaultTenant().toBuilder()
                .uuid(null)
                .build();
    }

    public static UserAccount  defaultUserAccount() {
        return UserAccount.builder()
                .uuid(UUID.randomUUID())
                .tenant(defaultTenant())
                .email("agente@prueba.com")
                .role(Role.AGENT)
                .isLocked(false)
                .deletedAt(null)
                .build();
    }

    public static Guest defaultGuest() {
        return Guest.builder()
                .uuid(UUID.randomUUID())
                .tenant(defaultTenant())
                .name("Test Guest")
                .email("test@guest.com")
                .deletedAt(null)
                .build();
    }

    public static Room defaultRoom() {
        return Room.builder()
                .uuid(UUID.randomUUID())
                .tenant(defaultTenant())
                .guest(defaultGuest())
                .agent(null)
                .status(RoomStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .closedAt(null)
                .deletedAt(null)
                .build();
    }

    public static Agent defaultAgent() {
        return Agent.builder()
                .uuid(UUID.randomUUID())
                .userAccount(defaultUserAccount())
                .department("Ventas")
                .displayName("AgentDisplayName")
                .deletedAt(null)
                .build();
    }

    public static Credential defaultCredential() {
        return Credential.builder()
                .id(1L)
                .userAccount(defaultUserAccount())
                .passwordHash("FakePasswordHash")
                .provider(AuthProvider.DEFAULT)
                .build();
    }

}
