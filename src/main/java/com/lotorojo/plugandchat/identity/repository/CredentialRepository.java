package com.lotorojo.plugandchat.identity.repository;

import com.lotorojo.plugandchat.identity.entity.AuthProvider;
import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import org.hibernate.annotations.processing.SQL;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, Long> {


    @Query("SELECT c FROM Credential c " +
            "JOIN FETCH c.userAccount u " +
            "WHERE u.email = :email " +
            "AND u.tenant.uuid = :tenantId " +
            "AND c.provider = :provider")
    Optional<Credential> findByUserAccountEmailAndUserAccountTenantUuidAndProvider(
            @Param("email") String email,
            @Param("tenantId") UUID tenantId,
            @Param("provider") AuthProvider provider);

}
