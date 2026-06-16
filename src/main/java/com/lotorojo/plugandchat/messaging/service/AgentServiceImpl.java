package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.core.exception.DuplicateAgentException;
import com.lotorojo.plugandchat.core.exception.NonExistingTenantException;
import com.lotorojo.plugandchat.core.tenant.TenantContext;
import com.lotorojo.plugandchat.identity.dto.CreateCredentialDTO;
import com.lotorojo.plugandchat.identity.dto.CreateUserAccountDTO;
import com.lotorojo.plugandchat.identity.entity.Role;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.identity.repository.UserAccountRepository;
import com.lotorojo.plugandchat.identity.service.CredentialService;
import com.lotorojo.plugandchat.identity.service.UserAccountService;
import com.lotorojo.plugandchat.messaging.dto.AgentResponse;
import com.lotorojo.plugandchat.messaging.dto.RegisterAgentRequest;
import com.lotorojo.plugandchat.messaging.entity.Agent;
import com.lotorojo.plugandchat.messaging.mapper.AgentMapper;
import com.lotorojo.plugandchat.messaging.repository.AgentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AgentServiceImpl implements AgentService {

    private final UserAccountRepository userAccountRepository;
    private final UserAccountService userAccountService;
    private final CredentialService credentialService;
    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;

    public AgentServiceImpl(UserAccountRepository userAccountRepository,
                            UserAccountService userAccountService,
                            CredentialService credentialService,
                            AgentRepository agentRepository,
                            AgentMapper agentMapper) {
        this.userAccountRepository = userAccountRepository;
        this.userAccountService = userAccountService;
        this.credentialService = credentialService;
        this.agentRepository = agentRepository;
        this.agentMapper = agentMapper;
    }

    @Override
    @Transactional
    public AgentResponse registerAgent(RegisterAgentRequest request) {
        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new NonExistingTenantException("El ID del tenant esta vacio");
        }

        if (userAccountRepository.existsByEmailAndTenantUuid(request.email(), tenantId)) {
            throw new DuplicateAgentException("El email " + request.email() + " ya se encuentra registrado para este tenant.");
        }

        CreateUserAccountDTO userAccountDTO = new CreateUserAccountDTO(
                tenantId,
                request.email(),
                Role.AGENT
        );
        UserAccount userAccount = userAccountService.createUserAccount(userAccountDTO);

        CreateCredentialDTO credentialDTO = new CreateCredentialDTO(
                userAccount.getUuid(),
                request.password(),
                "DEFAULT"
        );
        credentialService.createCredential(credentialDTO);

        Agent agent = new Agent(userAccount, request.department(), request.displayName());
        Agent savedAgent = agentRepository.save(agent);

        return agentMapper.toDto(savedAgent);
    }

    @Override
    public Agent getAgent(UUID agentId) {
        return agentRepository.getAgentOrThrow(agentId);
    }
}

