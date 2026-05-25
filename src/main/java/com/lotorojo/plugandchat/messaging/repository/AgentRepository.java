package com.lotorojo.plugandchat.messaging.repository;

import com.lotorojo.plugandchat.messaging.entity.Agent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AgentRepository extends JpaRepository<Agent, UUID> {
    Optional<Agent> findByUserAccountUuid(UUID userAccountUuid);
}

