package ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "manifests")
@Data
@NoArgsConstructor
public class ManifestEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;
    
    @Column(length = 1000)
    private String description;
    
    private String owner;
    
    @Column(name = "user_id")
    private String userId;
    
    private boolean isPrivate;
    
    private LocalDateTime uploadedAt;
    
    private Long downloadCount = 0L;

    public ManifestEntity(String name, String description, String owner, String userId, boolean isPrivate) {
        this.name = name;
        this.description = description;
        this.owner = owner;
        this.userId = userId;
        this.isPrivate = isPrivate;
        this.uploadedAt = LocalDateTime.now();
    }
}
