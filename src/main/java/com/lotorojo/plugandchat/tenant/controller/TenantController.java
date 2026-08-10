package com.lotorojo.plugandchat.tenant.controller;

import com.lotorojo.plugandchat.identity.service.UserAccountService;
import com.lotorojo.plugandchat.tenant.dto.CreateTenantRequest;
import com.lotorojo.plugandchat.tenant.dto.TenantResponse;
import com.lotorojo.plugandchat.tenant.mapper.TenantMapper;
import com.lotorojo.plugandchat.tenant.service.TenantService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService){
        this.tenantService = tenantService;
    }


    @PostMapping("/tenant")
    public ResponseEntity<TenantResponse> createTenant(@RequestBody CreateTenantRequest createTenantRequest){

        TenantResponse tenantResponse = tenantService.provisionNewTenant(createTenantRequest);
        return new ResponseEntity<>(tenantResponse, HttpStatus.CREATED);

    }

    @DeleteMapping("/tenant/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable UUID id){

        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();

    }

    @GetMapping("/tenant/lookup")
    public ResponseEntity<TenantResponse> lookupTenant(@RequestParam String name){

        TenantResponse tenantResponse = tenantService.getTenantByName(name);
        return new ResponseEntity<>(tenantResponse, HttpStatus.OK);
    }


}
