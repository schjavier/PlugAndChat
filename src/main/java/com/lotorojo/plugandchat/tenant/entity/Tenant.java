package com.lotorojo.plugandchat.tenant.entity;


import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name="tenant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID uuid;

    private String name;
    private String api_key;

    public Tenant(String name, String api_key){
        this.name = name;
        this.api_key = api_key;
    }
}
