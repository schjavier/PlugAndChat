package com.lotorojo.plugandchat.tenant.service;

import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.repository.TenantRepository;
import com.lotorojo.plugandchat.tenant.util.ApiKeyGenerator;
import com.lotorojo.plugandchat.tenant.validations.TenantValidations;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TenantServiceImpl implements TenantService{

    private final TenantRepository tenantRepository;
    private final TenantValidations tenantValidations;

    public TenantServiceImpl(TenantRepository tenantRepository, TenantValidations tenantValidations){
        this.tenantRepository = tenantRepository;
        this.tenantValidations = tenantValidations;
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

        Tenant tenant = tenantValidations.getTenantOrThrow(id);
        tenantRepository.delete(tenant);

    }
}
