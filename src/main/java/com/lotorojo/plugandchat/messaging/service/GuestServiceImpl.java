package com.lotorojo.plugandchat.messaging.service;

import com.lotorojo.plugandchat.messaging.entity.Guest;
import com.lotorojo.plugandchat.messaging.entity.Room;
import com.lotorojo.plugandchat.messaging.repository.GuestRepository;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class GuestServiceImpl implements GuestService {

    private GuestRepository guestRepository;

    public GuestServiceImpl(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Override
    @Transactional
    public Guest getOrCreateGuest(String name, String email, Tenant tenant) {
        return guestRepository.findByEmailAndTenantUuid(email, tenant.getUuid()).orElseGet(
                () -> {
                    Guest newGuest = new Guest(tenant, name, email);
                    return guestRepository.save(newGuest);
                }
        );

    }
}
