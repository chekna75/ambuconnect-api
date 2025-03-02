package fr.ambuconnect.messagerie.entity;

import java.util.UUID;
import java.time.LocalDateTime;
import fr.ambuconnect.messagerie.enums.UserType;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "messages")
public class MessagerieEntity  extends PanacheEntityBase{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "sender_id")
    private UUID senderId;

    @Column(name = "receiver_id")
    private UUID receiverId;

    @Column(name = "content")
    private String content;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "sender_type")
    @Enumerated(EnumType.STRING)
    private UserType senderType;  // admin ou chauffeur

    @Column(name = "receiver_type")
    @Enumerated(EnumType.STRING)
    private UserType receiverType; // admin ou chauffeur

    // Getters et setters
}
