package com.lotorojo.plugandchat.messaging.controller;


import com.lotorojo.plugandchat.TestDataFactory;
import com.lotorojo.plugandchat.core.security.JwtService;
import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.Role;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.identity.repository.CredentialRepository;
import com.lotorojo.plugandchat.identity.repository.UserAccountRepository;
import com.lotorojo.plugandchat.messaging.dto.AssignAgentRequest;
import com.lotorojo.plugandchat.messaging.dto.CreateRoomRequest;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.entity.RoomStatus;
import com.lotorojo.plugandchat.messaging.repository.AgentRepository;
import com.lotorojo.plugandchat.messaging.repository.GuestRepository;
import com.lotorojo.plugandchat.messaging.repository.RoomRepository;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class RoomControllerIntegrationTest {

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private GuestRepository guestRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private CredentialRepository credentialRepository;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Tenant testTenant;
    private CreateRoomRequest createRoomRequest;
    private Guest testGuest;
    private Room testRoom;
    private Agent testAgent;
    private UserAccount testUserAccount;
    private Credential testCredential;
    private AssignAgentRequest testAssignAgentRequest;
    private UserDetails testUserDetails;
    private String token;


    @BeforeEach
    public void setup() {

        this.testTenant = TestDataFactory.newTenantRequest();
        this.tenantRepository.save(testTenant);

        this.createRoomRequest = new CreateRoomRequest("Test Guest", "test@guest.com");

        this.testGuest = TestDataFactory.defaultGuest()
                .toBuilder()
                .uuid(null)
                .tenant(testTenant)
                .build();
        this.guestRepository.save(testGuest);

        this.testRoom = TestDataFactory.defaultRoom()
                .toBuilder()
                .uuid(null)
                .tenant(testTenant)
                .guest(testGuest)
                .build();
        this.roomRepository.save(this.testRoom);

        testUserAccount = TestDataFactory.defaultUserAccount()
                .toBuilder()
                .uuid(null)
                .tenant(testTenant)
                .build();
        userAccountRepository.save(testUserAccount);

        this.testAgent = TestDataFactory
                .defaultAgent()
                .toBuilder()
                .uuid(null)
                .userAccount(testUserAccount)
                .build();
        agentRepository.save(this.testAgent);

        this.testCredential = TestDataFactory.defaultCredential()
                .toBuilder()
                .id(null)
                .userAccount(testUserAccount)
                .build();
        this.credentialRepository.save(testCredential);

        this.testAssignAgentRequest = new AssignAgentRequest(testRoom.getUuid(), testAgent.getUuid());

        this.testUserDetails = User.builder()
                .username(testUserAccount.getEmail())
                .password("FakePasswordHash")
                .roles(testUserAccount.getRole().name())
                .build();

        this.token = jwtService.generateToken(testUserDetails, testTenant.getUuid());
    }

    @Test
    public void shouldCreateRoomSuccessfully() throws Exception {

        mockMvc.perform(post("/rooms")
                        .header("X-Tenant-ID", testTenant.getUuid().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.uuid", notNullValue()))
                .andExpect(jsonPath("$.tenantId", is(testTenant.getUuid().toString())))
                .andExpect(jsonPath("$.guestName", is("Test Guest")))
                .andExpect(jsonPath("$.token",  notNullValue()))
                .andExpect(jsonPath("$.status", is("WAITING")));

    }

    @Test
    public void shouldReturnBadRequestWhenTenantIdIsMissing() throws Exception {

        mockMvc.perform(post("/rooms")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void shouldReturnBadRequestWhenTenantIdIsEmpty() throws Exception {

        mockMvc.perform(post("/rooms")
                        .header("X-Tenant-ID", " ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldReturnBadRequestWhenTenantIdIsInvalid() throws Exception {

        mockMvc.perform(post("/rooms")
                        .header("X-Tenant-ID", "Invalid_UUID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRoomRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Error de Formato")))
                .andExpect(jsonPath("$.detail", is("Tenant ID invalido")));
    }

    @Test
    public void shouldAssignAgentSuccessfully() throws Exception {

        mockMvc.perform(post("/rooms/assign")
                        .header("X-Tenant-ID", testTenant.getUuid().toString())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAssignAgentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid", notNullValue()))
                .andExpect(jsonPath("$.tenantId", is(testTenant.getUuid().toString())))
                .andExpect(jsonPath("$.guestId", is(testGuest.getUuid().toString())))
                .andExpect(jsonPath("$.guestName", is("Test Guest")))
                .andExpect(jsonPath("$.agentId", is(testAgent.getUuid().toString())))
                .andExpect(jsonPath("$.status", is(RoomStatus.ACTIVE.name())))
                .andExpect(jsonPath("$.createdAt", is(testRoom.getCreatedAt().toString())));
    }

    @Test
    public void shouldThrowExceptionWhenTokenIsNotPresent() throws Exception {
        mockMvc.perform(post("/rooms/assign")
                .header("X-Tenant-ID", testTenant.getUuid().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testAssignAgentRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void shouldThrowExceptionWhenTokenIsNotValid() throws Exception {
        mockMvc.perform(post("/rooms/assign")
                        .header("X-Tenant-ID", testTenant.getUuid().toString())
                        .header("Authorization", "Bearer " + "InvaldidToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testAssignAgentRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail", is("Token Invalido o Expirado")))
                .andExpect(jsonPath("$.title", is("Authentication Error")));
    }

    @Test
    public void shouldFailToAssignAgentWhenRoomBelongsToOtherTenant() throws Exception {
        Tenant tenantB = new Tenant("tenantB", "apiKeyB");
        tenantRepository.save(tenantB);

        Guest guestB = new Guest(tenantB, "GuestB", "guestB@prueba.com");
        guestRepository.save(guestB);

        Room roomB = new Room(tenantB, guestB, RoomStatus.WAITING, LocalDateTime.now());
        roomRepository.save(roomB);

        AssignAgentRequest crossTenantRequest = new AssignAgentRequest(roomB.getUuid(), testAgent.getUuid());

        mockMvc.perform(post("/rooms/assign")
                .header("X-Tenant-ID", testTenant.getUuid().toString())
                .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crossTenantRequest)))
                .andExpect(status().isUnauthorized());

    }

    @Test
    public void shouldFailToAssignAgentWhenAgentBelongsToOtherTenant() throws Exception {
        Tenant tenantB = new Tenant("tenantB", "apiKeyB");
        tenantRepository.save(tenantB);

        UserAccount userAccountB = new UserAccount(tenantB, "agenteB@prueba.com", Role.AGENT, false);
        userAccountRepository.save(userAccountB);

        Agent agentB = new Agent(userAccountB, "soporte", "AgenteB");
        agentRepository.save(agentB);

        AssignAgentRequest crossTenantRequest = new AssignAgentRequest(testRoom.getUuid(), agentB.getUuid());

        mockMvc.perform(post("/rooms/assign")
                        .header("X-Tenant-ID", testTenant.getUuid().toString())
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(crossTenantRequest)))
                .andExpect(status().isUnauthorized());
    }

}
