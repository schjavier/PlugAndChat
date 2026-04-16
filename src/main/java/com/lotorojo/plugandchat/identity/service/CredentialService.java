package com.lotorojo.plugandchat.identity.service;

import com.lotorojo.plugandchat.identity.dto.CreateCredentialDTO;
import com.lotorojo.plugandchat.identity.entity.Credential;

public interface CredentialService {

    Credential createCredential(CreateCredentialDTO createCredentialDTO)  ;

}
