package com.lotorojo.plugandchat.tenant.controller;

import com.lotorojo.plugandchat.tenant.dto.TenantResponse;
import com.lotorojo.plugandchat.tenant.entity.Tenant;
import com.lotorojo.plugandchat.tenant.mapper.TenantMapper;
import com.lotorojo.plugandchat.tenant.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TenantController {

    private final TenantService tenantService;
    private final TenantMapper tenantMapper;

    public TenantController(TenantService tenantService, TenantMapper tenantMapper){
        this.tenantService = tenantService;
        this.tenantMapper = tenantMapper;
    }

    @PostMapping("/tenant")
    public ResponseEntity<TenantResponse> createTenant(@RequestBody String name){

        TenantResponse tenantResponse = tenantMapper.toDto(tenantService.createTenant(name));

        return new ResponseEntity<>(tenantResponse, HttpStatus.CREATED);

    }


}
