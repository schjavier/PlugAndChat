package com.lotorojo.plugandchat.identity.service;

import com.lotorojo.plugandchat.identity.dto.LoginRequest;
import com.lotorojo.plugandchat.identity.dto.LoginResponse;
import org.springframework.stereotype.Service;


public interface AuthService {

    LoginResponse login(LoginRequest loginRequest);

}
