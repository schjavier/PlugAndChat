package com.lotorojo.plugandchat.messaging.entity;

import com.lotorojo.plugandchat.identity.entity.UserAccount;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "agent")
@SQLDelete(sql = "UPDATE agent set deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Agent {


    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID uuid;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserAccount userAccount;

    private String department;
    private String displayName;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public Agent(UserAccount userAccount, String department, String displayName) {
        this.userAccount = userAccount;
        this.department = department;
        this.displayName = displayName;
    }

}
