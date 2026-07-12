package com.lotorojo.plugandchat.messaging.entity;

import com.lotorojo.plugandchat.tenant.entity.Tenant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "guest")
@SQLDelete(sql = "UPDATE guest set deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    private String name;
    private String email;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Guest(Tenant tenant, String name, String email) {
        this.tenant = tenant;
        this.name = name;
        this.email = email;

    }

}




