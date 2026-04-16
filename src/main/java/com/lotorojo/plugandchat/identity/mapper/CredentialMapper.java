package com.lotorojo.plugandchat.identity.mapper;

import com.lotorojo.plugandchat.identity.entity.Credential;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import org.springframework.stereotype.Component;


@Component
public class CredentialMapper {

    public Credential toEntity(UserAccount userAccount, String encryptedPassword, String provider) {

        return new Credential(
                userAccount,
                encryptedPassword,
                provider);
    }

}
