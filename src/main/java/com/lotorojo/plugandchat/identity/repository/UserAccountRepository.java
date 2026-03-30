package com.lotorojo.plugandchat.identity.repository;

import com.lotorojo.plugandchat.identity.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserAccountRepository extends JpaRepository<UserAccount, UUID> {



}
