package com.lotorojo.plugandchat.identity.repository;

import com.lotorojo.plugandchat.core.exception.NoUserAccountFoundException;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {
    default UserAccount getUserAccountOrThrow(UUID uuid) {
        return findById(uuid).orElseThrow(
                () -> new NoUserAccountFoundException("La Cuenta de Usuario con el ID: " + uuid + " no se encuentra")
        );


    }

    Optional<UserAccount> findByEmailAndUuid(String email, UUID tenanUuid);

    boolean existsByEmailAndTenantUuid(String email, UUID tenantUuid);
}

