package com.lotorojo.plugandchat.tenant.validations;

import com.lotorojo.plugandchat.core.exception.DuplicateNameException;
import com.lotorojo.plugandchat.core.exception.NonExistingTenantException;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.repository.TenantRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TenantValidations {

    private final TenantRepository tenantRepository;

    public TenantValidations(TenantRepository tenantRepository){
        this.tenantRepository = tenantRepository;

    }

    public void validateName(String name){

        if (tenantRepository.existsByName(name)){
            throw new DuplicateNameException("El Nombre ya se encuentra registrado");
        }

    }

}
