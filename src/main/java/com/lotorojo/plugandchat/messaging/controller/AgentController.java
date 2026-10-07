package com.lotorojo.plugandchat.messaging.controller;

import com.lotorojo.plugandchat.core.tenant.TenantContext;
import com.lotorojo.plugandchat.messaging.dto.AgentResponse;
import com.lotorojo.plugandchat.messaging.dto.RegisterAgentRequest;
import com.lotorojo.plugandchat.messaging.service.AgentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

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

    @GetMapping("/agents")
    public ResponseEntity<List<AgentResponse>> getAllAgents() {
        UUID tenantUuid = TenantContext.getCurrentTenant();
        List<AgentResponse> agentResponses = agentService.getAgentsByTenantUuid(tenantUuid);
        return ResponseEntity.ok(agentResponses);
    }

    @GetMapping("/agents/active")
    public ResponseEntity<Integer> getActiveAgents() {
        UUID tenantUuid = TenantContext.getCurrentTenant();
        Integer activeAgents = agentService.countActiveAgentsByTenantUuid(tenantUuid);
        return ResponseEntity.ok(activeAgents);
    }
}
