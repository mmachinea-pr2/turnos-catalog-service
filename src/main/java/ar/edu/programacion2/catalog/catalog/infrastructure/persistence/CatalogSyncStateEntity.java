package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "catalog_sync_state")
public class CatalogSyncStateEntity {
    @Id
    private Integer id;
    private Long appliedVersion;
    private Instant appliedAt;

    protected CatalogSyncStateEntity() {}

    public Integer getId() { return id; }
    public Long getAppliedVersion() { return appliedVersion; }
    public Instant getAppliedAt() { return appliedAt; }

    // La futura sincronización debe invocarlo en la misma transacción que los datos.
    public void markApplied(long version, Instant appliedAt) {
        if (version < 0 || appliedAt == null) {
            throw new IllegalArgumentException("Version and application time are required");
        }
        if (appliedVersion != null && version < appliedVersion) {
            throw new IllegalArgumentException("Catalog version cannot move backwards");
        }
        this.appliedVersion = version;
        this.appliedAt = appliedAt;
    }
}
