package com.lotorojo.plugandchat.identity.service;

import com.lotorojo.plugandchat.identity.dto.CreateUserAccountDTO;
import com.lotorojo.plugandchat.identity.entity.UserAccount;

public interface UserAccountService {

UserAccount createUserAccount(CreateUserAccountDTO data);


}
