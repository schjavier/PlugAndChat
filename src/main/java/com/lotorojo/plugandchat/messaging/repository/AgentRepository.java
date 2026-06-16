package com.lotorojo.plugandchat.messaging.repository;

import com.lotorojo.plugandchat.core.exception.NoAgentFoundException;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
    default Agent getAgentOrThrow(UUID agentId) {
        return findById(agentId).orElseThrow(
                () -> new NoAgentFoundException("No se puedo encontrar el Agente"));
    }

    Optional<Agent> findByUserAccountUuid(UUID userAccountUuid);
}

