package fr.ambuconnect.notification.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.ambuconnect.courses.entity.CoursesEntity;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEntity extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "destinataire_id", nullable = false)
    private UUID destinataireId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private CoursesEntity course;

    @Column(name = "date_creation", nullable = false)
    private LocalDateTime dateCreation;

    @Column(name = "lue", nullable = false)
    private boolean lue;
} 