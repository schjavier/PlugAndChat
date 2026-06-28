package com.lotorojo.plugandchat.messaging.controller;


import com.lotorojo.plugandchat.messaging.dto.CreateRoomRequest;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.repository.TenantRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

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
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private Tenant testTenant;
    private CreateRoomRequest createRoomRequest;

    @BeforeEach
    public void setup() {

        testTenant = new Tenant("testTenant", "Api_key");
        tenantRepository.save(testTenant);

        this.createRoomRequest = new CreateRoomRequest("Javier", "example@gmail.com");

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
                .andExpect(jsonPath("$.guestName", is("Javier")))
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


}
