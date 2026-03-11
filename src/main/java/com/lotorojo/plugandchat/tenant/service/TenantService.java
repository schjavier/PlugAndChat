package com.lotorojo.plugandchat.tenant.service;

import com.lotorojo.plugandchat.tenant.entity.Tenant;

import java.util.UUID;

public interface TenantService {

    Tenant createTenant(String name);
    boolean deleteTenant(UUID id);


}
