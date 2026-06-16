package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.tenant.entity.Tenant;

public interface GuestService {

    Guest getOrCreateGuest(String name, String email, Tenant tenant);

}
