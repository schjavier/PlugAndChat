package com.lotorojo.plugandchat.tenant.service;

import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.repository.TenantRepository;
import com.lotorojo.plugandchat.tenant.util.ApiKeyGenerator;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TenantServiceImpl implements TenantService{

    private final TenantRepository tenantRepository;

    public TenantServiceImpl(TenantRepository tenantRepository){
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Tenant createTenant(String name) {

        //todo validar si existe el nombre

        String apiKey = ApiKeyGenerator.generateApiKey();
        Tenant tenant = new Tenant(name, apiKey);
        return tenantRepository.save(tenant);

    }

    @Override
    public boolean deleteTenant(UUID id) {
        return false;
    }
}
