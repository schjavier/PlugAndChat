package com.lotorojo.plugandchat.messaging.mapper;

import com.lotorojo.plugandchat.messaging.dto.AgentResponse;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import org.springframework.stereotype.Component;

@Component
public class AgentMapper {

    public AgentResponse toDto(Agent agent) {
        if (agent == null) {
            return null;
        }
        return new AgentResponse(
                agent.getUuid(),
                agent.getUserAccount().getEmail(),
                agent.getDepartment(),
                agent.getDisplayName()
        );
    }
}

