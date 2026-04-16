package com.lotorojo.plugandchat.tenant.service;

import com.lotorojo.plugandchat.identity.dto.CreateCredentialDTO;
import com.lotorojo.plugandchat.identity.dto.CreateUserAccountDTO;
import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.Role;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.identity.service.CredentialService;
import com.lotorojo.plugandchat.identity.service.UserAccountService;
import com.lotorojo.plugandchat.tenant.dto.CreateTenantRequest;
import com.lotorojo.plugandchat.tenant.dto.TenantResponse;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.mapper.TenantMapper;
import com.lotorojo.plugandchat.tenant.repository.TenantRepository;
import com.lotorojo.plugandchat.tenant.util.ApiKeyGenerator;
import com.lotorojo.plugandchat.tenant.validations.TenantValidations;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TenantServiceImpl implements TenantService{

    private final TenantRepository tenantRepository;
    private final TenantValidations tenantValidations;
    private final UserAccountService userAccountService;
    private final TenantMapper tenantMapper;
    private final CredentialService credentialService;

    public TenantServiceImpl(TenantRepository tenantRepository, TenantValidations tenantValidations, UserAccountService userAccountService, TenantMapper tenantMapper, CredentialService credentialService){
        this.tenantRepository = tenantRepository;
        this.tenantValidations = tenantValidations;
        this.userAccountService = userAccountService;
        this.tenantMapper = tenantMapper;
        this.credentialService = credentialService;
    }

    @Override
    public Tenant createTenant(String name) {

        tenantValidations.validateName(name);

        String apiKey = ApiKeyGenerator.generateApiKey();
        Tenant tenant = new Tenant(name, apiKey);
        return tenantRepository.save(tenant);

    }

    @Override
    public void deleteTenant(UUID id) {

        Tenant tenant = tenantRepository.getTenantOrThrow(id);
        tenantRepository.delete(tenant);

    }

    @Override
    public Tenant getTenant(UUID tenant_id) {

        return tenantRepository.getTenantOrThrow(tenant_id);

    }

    @Override
    @Transactional
    public TenantResponse provisionNewTenant(CreateTenantRequest request) {
        Tenant tenant = createTenant(request.name());

        CreateUserAccountDTO adminDto = new CreateUserAccountDTO(
                tenant.getUuid(),
                request.adminEmail(),
                Role.ADMIN
        );

        UserAccount admin = userAccountService.createUserAccount(adminDto);

        CreateCredentialDTO credentialDTO = new CreateCredentialDTO(
                admin.getUuid(),
                request.adminPassword(),
                "DEFAULT"
        );

        credentialService.createCredential(credentialDTO);

        return tenantMapper.toDto(tenant);
    }
}
