package com.lotorojo.plugandchat.tenant.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name="tenant")
@Getter
@Setter
@EqualsAndHashCode(of="uuid")
public class Tenant {

    @Id
    private UUID uuid;

    private String name;
    private String api_key;


    public Tenant(String name, String api_key) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.api_key = api_key;
    }

    public Tenant() {
    }


}
