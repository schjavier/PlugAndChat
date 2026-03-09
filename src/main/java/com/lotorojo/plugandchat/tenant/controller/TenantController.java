package com.lotorojo.plugandchat.tenant.controller;

import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService){
        this.tenantService = tenantService;
    }

    @PostMapping("/tenant")
    public ResponseEntity<Tenant> createTenant(@RequestBody String name){
        Tenant tenant = tenantService.createTenant(name);
        return new ResponseEntity<>(tenant, HttpStatus.CREATED);

    }


}
