package com.lotorojo.plugandchat.identity.service;

import com.lotorojo.plugandchat.identity.dto.CreateUserAccountDTO;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.identity.mapper.UserAccountMapper;
import com.lotorojo.plugandchat.identity.repository.UserAccountRepository;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.repository.TenantRepository;
import com.lotorojo.plugandchat.tenant.service.TenantService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserAccountServiceImpl implements UserAccountService{

    private final UserAccountMapper userAccountMapper;
    private final TenantRepository tenantRepository;
    private final UserAccountRepository userAccountRepository;

    public UserAccountServiceImpl(UserAccountMapper userAccountMapper,
                                  TenantRepository tenantRepository,
                                  UserAccountRepository userAccountRepository){

        this.userAccountMapper = userAccountMapper;
        this.tenantRepository = tenantRepository;
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional
    public UserAccount createUserAccount(CreateUserAccountDTO data) {

        Tenant tenant = tenantRepository.getTenantOrThrow(data.tenantId());
        UserAccount userAccount = userAccountMapper.toEntity(data, tenant);

        return userAccountRepository.save(userAccount);
    }

    @Override
    public UserAccount getUserAccount(UUID accountId) {
        return userAccountRepository.getUserAccountOrThrow(accountId);
    }
}
