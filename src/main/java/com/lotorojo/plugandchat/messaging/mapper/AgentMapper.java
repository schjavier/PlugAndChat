package com.lotorojo.plugandchat.messaging.mapper;

import com.lotorojo.plugandchat.messaging.dto.AgentResponse;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class AgentMapper {

    public AgentResponse toDto(@NotNull Agent agent) {

        return new AgentResponse(
                agent.getUuid(),
                agent.getUserAccount().getEmail(),
                agent.getDepartment(),
                agent.getDisplayName()
        );
    }
}

