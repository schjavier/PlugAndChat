package com.lotorojo.plugandchat.tenant.repository;

import com.lotorojo.plugandchat.core.exception.NonExistingTenantException;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
    default Tenant getTenantOrThrow(UUID id) {
        return findById(id)
                .orElseThrow(()-> new NonExistingTenantException("El Tenant con el ID: " + id + " no existe"));

    }

    Optional<Tenant> findByNameIgnoreCase(String name);
    boolean existsByName(String name);

}
