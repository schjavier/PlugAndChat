package com.lotorojo.plugandchat.identity.mapper;

import com.lotorojo.plugandchat.identity.dto.CreateUserAccountDTO;
import com.lotorojo.plugandchat.identity.entity.UserAccount;
import com.lotorojo.plugandchat.identity.service.UserAccountService;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import org.springframework.stereotype.Component;


@Component
public class UserAccountMapper {

   public UserAccount toEntity(CreateUserAccountDTO data, Tenant tenant){

   return new UserAccount(
           tenant,
           data.email(),
           data.role(),
           false
        );
   }

}
