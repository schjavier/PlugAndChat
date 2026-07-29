package com.lotorojo.plugandchat.identity.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "credential")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Credential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount userAccount;

    private String passwordHash;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider;


    public Credential(UserAccount userAccount, String passwordHash, String provider) {
        this.userAccount = userAccount;
        this.passwordHash = passwordHash;
        this.provider = AuthProvider.valueOf(provider);
    }
}
