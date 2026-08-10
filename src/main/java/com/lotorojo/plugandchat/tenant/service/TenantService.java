package com.lotorojo.plugandchat.tenant.service;

import com.lotorojo.plugandchat.tenant.dto.CreateTenantRequest;
import com.lotorojo.plugandchat.tenant.dto.TenantResponse;
import com.lotorojo.plugandchat.tenant.entity.Tenant;


import java.util.UUID;

public interface TenantService {

    Tenant createTenant(String name);
    void deleteTenant(UUID id);
    Tenant getTenant(UUID tenant_id);
    TenantResponse provisionNewTenant(CreateTenantRequest  request);
    TenantResponse getTenantByName(String name);

}
