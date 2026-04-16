package com.lotorojo.plugandchat.identity.service;

import com.lotorojo.plugandchat.identity.dto.CreateUserAccountDTO;
import com.lotorojo.plugandchat.identity.entity.UserAccount;

import java.util.UUID;

public interface UserAccountService {

UserAccount createUserAccount(CreateUserAccountDTO data);
UserAccount getUserAccount(UUID accountId);


}
