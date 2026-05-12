package com.lotorojo.plugandchat.core.security;

import com.lotorojo.plugandchat.core.exception.NoUserAccountFoundException;
import com.lotorojo.plugandchat.core.exception.NonExistingTenantException;
import com.lotorojo.plugandchat.core.tenant.TenantContext;
import com.lotorojo.plugandchat.identity.entity.AuthProvider;
import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.identity.repository.CredentialRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CredentialRepository credentialRepository;

    public UserDetailsServiceImpl(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {

        UUID tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            throw new NonExistingTenantException("El ID del tenant esta vacio");
        }

       Credential credential = credentialRepository.findByUserAccountEmailAndUserAccountTenantUuidAndProvider(email, tenantId, AuthProvider.DEFAULT)
               .orElseThrow(() -> new NoUserAccountFoundException("No se Encuentra el usuario para el mail: " + email));

        UserAccount userAccount = credential.getUserAccount();

        GrantedAuthority authority = new SimpleGrantedAuthority(userAccount.getRole().name());

        return org.springframework.security.core.userdetails.User.builder()
                .username(userAccount.getEmail())
                .password(credential.getPasswordHash())
                .roles(userAccount.getRole().name()).build();
    }
}
