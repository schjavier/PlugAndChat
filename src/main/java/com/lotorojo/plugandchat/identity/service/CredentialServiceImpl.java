package com.lotorojo.plugandchat.identity.service;

import com.lotorojo.plugandchat.identity.dto.CreateCredentialDTO;
import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.identity.mapper.CredentialMapper;
import com.lotorojo.plugandchat.identity.repository.CredentialRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CredentialServiceImpl implements CredentialService {

    UserAccountService userAccountService;
    CredentialMapper credentialMapper;
    CredentialRepository credentialRepository;
    PasswordEncoder passwordEncoder;

    public CredentialServiceImpl(UserAccountService userAccountService,
                                 CredentialMapper credentialMapper,
                                 CredentialRepository credentialRepository,
                                 PasswordEncoder passwordEncoder) {

        this.userAccountService = userAccountService;
        this.credentialMapper = credentialMapper;
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Credential createCredential(CreateCredentialDTO createCredentialDTO) {

        UserAccount userAccount = userAccountService.getUserAccount(createCredentialDTO.UserAccountID());

        String encodedPassword = passwordEncoder.encode(createCredentialDTO.password());
        Credential credential = credentialMapper.toEntity(userAccount, encodedPassword, createCredentialDTO.provider());

        return credentialRepository.save(credential);
    }
}
