package com.lotorojo.plugandchat.identity.entity;

import com.lotorojo.plugandchat.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="user_account")
@SQLDelete(sql = "UPDATE plug_and_chat.user_account set deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    private boolean isLocked;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;


    public UserAccount(Tenant tenant, String email, Role role, boolean isLocked){
        this.tenant = tenant;
        this.email = email;
        this.role = role;
        this.isLocked = isLocked;
    }
}

