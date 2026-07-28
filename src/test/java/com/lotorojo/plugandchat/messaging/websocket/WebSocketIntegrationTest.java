package com.lotorojo.plugandchat.messaging.websocket;

import com.lotorojo.plugandchat.TestDataFactory;
import com.lotorojo.plugandchat.core.security.JwtService;
import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.identity.repository.CredentialRepository;
import com.lotorojo.plugandchat.identity.repository.UserAccountRepository;
import com.lotorojo.plugandchat.messaging.dto.ChatMessageRequest;
import com.lotorojo.plugandchat.messaging.dto.ChatMessageResponse;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Message;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.repository.AgentRepository;
import com.lotorojo.plugandchat.messaging.repository.GuestRepository;
import com.lotorojo.plugandchat.messaging.repository.MessageRepository;
import com.lotorojo.plugandchat.messaging.repository.RoomRepository;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.repository.TenantRepository;
import jakarta.validation.constraints.NotNull;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class WebSocketIntegrationTest {

    @LocalServerPort
    private Integer port;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TenantRepository tenantRepository;
    @Autowired
    private UserAccountRepository userAccountRepository;
    @Autowired
    private AgentRepository agentRepository;
    @Autowired
    private GuestRepository guestRepository;
    @Autowired
    private CredentialRepository credentialRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private JwtService jwtService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private WebSocketStompClient stompClient;
    private Tenant testTenant;
    private UserAccount testUserAccount;
    private Agent testAgent;
    private Credential testCredential;
    private String token;
    private String connectUrl;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private HandlerExceptionResolver handlerExceptionResolver;


    @BeforeEach
    void setup() {
        this.connectUrl = String.format("ws://localhost:%s/chat", port);

        this.stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        this.stompClient.setMessageConverter(new JacksonJsonMessageConverter());

        //Esto es para borrar el contenido de las tablas y que no fallen los tests por la Integridad Referencial
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE message");
        jdbcTemplate.execute("TRUNCATE TABLE agent");
        jdbcTemplate.execute("TRUNCATE TABLE credential");
        jdbcTemplate.execute("TRUNCATE TABLE user_account");
        jdbcTemplate.execute("TRUNCATE TABLE room");
        jdbcTemplate.execute("TRUNCATE TABLE guest");
        jdbcTemplate.execute("TRUNCATE TABLE tenant");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        this.testTenant = TestDataFactory.newTenantRequest();
        tenantRepository.save(testTenant);

        this.testUserAccount = TestDataFactory.defaultUserAccount()
                .toBuilder()
                .uuid(null)
                .tenant(testTenant)
                .build();
        userAccountRepository.save(testUserAccount);

        this.testAgent = TestDataFactory.defaultAgent()
                .toBuilder()
                .uuid(null)
                .userAccount(testUserAccount)
                .build();
        agentRepository.save(testAgent);

        this.testCredential = TestDataFactory.defaultCredential()
                .toBuilder()
                .id(null)
                .userAccount(testUserAccount)
                .build();
        credentialRepository.save(testCredential);

        UserDetails userDetails = User.builder()
                .username(testUserAccount.getEmail())
                .password(testCredential.getPasswordHash())
                .roles(testUserAccount.getRole().name())
                .build();

        this.token = jwtService.generateToken(userDetails, testTenant.getUuid());
    }


    @Test
    public void shouldConnectWhenHeadersArePresent() throws Exception {
        CompletableFuture<StompSession> future = new CompletableFuture<>();

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " + token);
        stompHeaders.add("X-Tenant-ID", testTenant.getUuid().toString());

        stompClient.connectAsync(connectUrl, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
           @Override
           public void afterConnected(@NonNull StompSession session, @NonNull StompHeaders stompHeaders) {
               future.complete(session);
           }
        });

        StompSession stompSession = future.get(3, TimeUnit.SECONDS);

        assertThat(stompSession).isNotNull();
        assertThat(stompSession.isConnected()).isTrue();

        stompSession.disconnect();

    }

    @Test
    public void shouldFailToConnectWhenAuthHeadersIsNotPresent() throws Exception {
        CompletableFuture<StompSession> future = new CompletableFuture<>();

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("X-Tenant-ID", testTenant.getUuid().toString());

        stompClient.connectAsync(connectUrl, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
           @Override
            public void handleTransportError(@NonNull StompSession stompSession,@NonNull Throwable exception) {
               future.completeExceptionally(exception);
           }
        });

        assertThrows(ExecutionException.class, () -> future.get(3, TimeUnit.SECONDS) );
    }

    @Test
    public void shouldConnectWhenAuthHeaderIsPresentButTenantHeaderIsNot() throws Exception {
        CompletableFuture<StompSession> future = new CompletableFuture<>();

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " + token);

        stompClient.connectAsync(connectUrl, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(@NonNull StompSession session, @NonNull StompHeaders stompHeaders) {
                future.complete(session);
            }
        });

        StompSession stompSession = future.get(3, TimeUnit.SECONDS);
        assertThat(stompSession).isNotNull();
        assertThat(stompSession.isConnected()).isTrue();

        stompSession.disconnect();

    }

    @Test
    public void shouldFailToConnectWhenTokenInvalid() throws Exception {
        CompletableFuture<StompSession> future = new CompletableFuture<>();

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " + "invalidToken");

        stompClient.connectAsync(connectUrl, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void handleTransportError(@NonNull StompSession stompSession, @NonNull Throwable exception) {
                future.completeExceptionally(exception);
            }
        });

        assertThrows(ExecutionException.class, () -> future.get(3, TimeUnit.SECONDS) );

    }

    @Test
    public void shouldConnectSuccessfullyAsGuest() throws Exception{
        CompletableFuture<StompSession> future = new CompletableFuture<>();

        String guestEmail = "prueba@guest.com";
        String guestToken = jwtService.generateGuestToken(guestEmail,  testTenant.getUuid());

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " + guestToken);

        stompClient.connectAsync(connectUrl, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void afterConnected(@NonNull StompSession session, @NonNull StompHeaders stompHeaders) {
                future.complete(session);
            }
        });

        StompSession stompSession = future.get(3, TimeUnit.SECONDS);
        assertThat(stompSession).isNotNull();
        assertThat(stompSession.isConnected()).isTrue();
        stompSession.disconnect();

    }

    @Test
    public void shouldFailToSubscribeToOtherTenantTopic() throws Exception {
        CompletableFuture<String> errorFuture = new CompletableFuture<>();

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " + token);

        StompSession session = stompClient.connectAsync(connectUrl, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                if (headers.containsKey("message")) {
                    errorFuture.complete(headers.getFirst("message"));
                }
            }

            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
                Throwable root = exception;
                while (root.getCause() != null) {
                    root = root.getCause();
                }
                errorFuture.complete(root.getMessage());
            }
        }).get(3, TimeUnit.SECONDS);

        UUID foreignTenantId = UUID.randomUUID();
        UUID randomRoomId = UUID.randomUUID();
        String foreignDestination = String.format("/topic/tenants/%s/rooms/%s", foreignTenantId, randomRoomId);

        session.subscribe(foreignDestination, new StompSessionHandlerAdapter() {});

        String errorMessage = errorFuture.get(3, TimeUnit.SECONDS);
        assertThat(errorMessage).contains("clientInboundChannel");

        if (session.isConnected()) {
            session.disconnect();
        }
    }

    @Test
    public void shouldPersistAndBroadcastMessageSuccessfully() throws Exception {

        Guest testGuest = TestDataFactory.defaultGuest().toBuilder().uuid(null).tenant(testTenant).build();
        guestRepository.save(testGuest);

        Room testRoom = TestDataFactory.defaultRoom().toBuilder().uuid(null).tenant(testTenant).guest(testGuest).build();
        roomRepository.save(testRoom);

        String guestToken = jwtService.generateGuestToken(testGuest.getEmail(), testTenant.getUuid());

        CompletableFuture<ChatMessageResponse> broadcastFuture = new CompletableFuture<>();

        StompHeaders stompHeaders = new StompHeaders();
        stompHeaders.add("Authorization", "Bearer " + guestToken);

        StompSession session = stompClient.connectAsync(connectUrl, new WebSocketHttpHeaders(), stompHeaders, new StompSessionHandlerAdapter() {
            @Override
            public void handleException(StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception){
                broadcastFuture.completeExceptionally(exception);
            }
        }).get(3, TimeUnit.SECONDS);

        String destinationTopic = String.format("/topic/tenants/%s/rooms/%s", testTenant.getUuid(), testRoom.getUuid());

        session.subscribe(destinationTopic, new StompFrameHandler() {
            @Override
            public Type getPayloadType(@NonNull StompHeaders headers) {
                return ChatMessageResponse.class;
            }

            @Override
            public void handleFrame(@NonNull StompHeaders headers, @Nullable Object payload) {
                broadcastFuture.complete( (ChatMessageResponse) payload);
            }
        });

        String destinationSend = String.format("/app/tenants/%s/rooms/%s/send", testTenant.getUuid(), testRoom.getUuid());
        ChatMessageRequest messageRequest = new ChatMessageRequest("Necesito Soporte, Por Favor");
        session.send(destinationSend, messageRequest);

        ChatMessageResponse receivedResponse = broadcastFuture.get(3, TimeUnit.SECONDS);
        assertThat(receivedResponse).isNotNull();
        assertThat(receivedResponse.content()).isEqualTo(messageRequest.content());
        assertThat(receivedResponse.senderName()).isEqualTo(testGuest.getName());

        List<Message> persistedMessages = messageRepository.findAll();
        assertThat(persistedMessages).hasSize(1);
        assertThat(persistedMessages.getFirst().getContent()).isEqualTo(messageRequest.content());
        assertThat(persistedMessages.getFirst().getRoom().getUuid()).isEqualTo(testRoom.getUuid());

        mockMvc.perform(get(String.format("/rooms/%s/messages", testRoom.getUuid()))
                .header("Authorization", "Bearer " + guestToken)
                .header("X-Tenant-ID", testTenant.getUuid().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content", is(messageRequest.content())));


        session.disconnect();

    }

}
