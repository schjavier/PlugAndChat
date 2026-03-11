package com.lotorojo.plugandchat.tenant.mapper;

import com.lotorojo.plugandchat.tenant.dto.TenantResponse;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {


    public TenantResponse toDto(Tenant tenant){
        return new TenantResponse(tenant.getUuid(), tenant.getName(), tenant.getApi_key());
    }

    public Tenant toEntity(TenantResponse tenantResponse){
        return new Tenant(tenantResponse.id(), tenantResponse.nombre(), tenantResponse.apiKey());
    }

}
