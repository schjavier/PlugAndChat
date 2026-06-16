package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.messaging.dto.AgentResponse;
import com.lotorojo.plugandchat.messaging.dto.RegisterAgentRequest;
import com.lotorojo.plugandchat.messaging.entity.Agent;

import java.util.UUID;

public interface AgentService {

    AgentResponse registerAgent(RegisterAgentRequest registerAgentRequest);
    Agent getAgent(UUID agentId);
}

