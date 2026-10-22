package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "professional")
public class ProfessionalEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String firstName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String lastName;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ProfessionalEntity() {}

    public ProfessionalEntity(Long id, Long categoryId, String firstName, String lastName, boolean enabled, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.categoryId = categoryId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public boolean getEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
