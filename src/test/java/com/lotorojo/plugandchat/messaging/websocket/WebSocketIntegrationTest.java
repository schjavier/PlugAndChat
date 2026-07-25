package com.lotorojo.plugandchat.messaging.websocket;

import com.lotorojo.plugandchat.TestDataFactory;
import com.lotorojo.plugandchat.core.security.JwtService;
import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.identity.repository.CredentialRepository;
import com.lotorojo.plugandchat.identity.repository.UserAccountRepository;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.repository.AgentRepository;
import com.lotorojo.plugandchat.messaging.repository.GuestRepository;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.repository.TenantRepository;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.converter.JacksonJsonMessageConverter;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.future;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebSocketIntegrationTest {

    @LocalServerPort
    private Integer port;

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

}
