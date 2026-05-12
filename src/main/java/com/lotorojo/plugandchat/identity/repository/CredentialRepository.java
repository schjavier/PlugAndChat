package com.lotorojo.plugandchat.identity.repository;

import com.lotorojo.plugandchat.identity.entity.AuthProvider;
import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import org.hibernate.annotations.processing.SQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, Long> {


    Optional<Credential> findByUserAccountEmailAndUserAccountTenantUuidAndProvider(
            String email,
            UUID tenantId,
            AuthProvider provider);

}
