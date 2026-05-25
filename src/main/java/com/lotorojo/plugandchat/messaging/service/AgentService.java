package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.messaging.dto.AgentResponse;
import com.lotorojo.plugandchat.messaging.dto.RegisterAgentRequest;

public interface AgentService {

    AgentResponse registerAgent(RegisterAgentRequest registerAgentRequest);
}

