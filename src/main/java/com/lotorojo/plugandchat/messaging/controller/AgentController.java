package com.lotorojo.plugandchat.messaging.controller;

import com.lotorojo.plugandchat.messaging.dto.AgentResponse;
import com.lotorojo.plugandchat.messaging.dto.RegisterAgentRequest;
import com.lotorojo.plugandchat.messaging.service.AgentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/agents")
    public ResponseEntity<AgentResponse> registerAgent(@RequestBody RegisterAgentRequest registerAgentRequest) {
        AgentResponse agentResponse = agentService.registerAgent(registerAgentRequest);
        return new ResponseEntity<>(agentResponse, HttpStatus.CREATED);
    }
}

