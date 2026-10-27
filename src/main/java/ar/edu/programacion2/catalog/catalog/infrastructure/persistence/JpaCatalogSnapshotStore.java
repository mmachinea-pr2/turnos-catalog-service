package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotStore;
import ar.edu.programacion2.catalog.catalog.domain.model.CatalogSnapshot;
import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCatalogSnapshotStore implements CatalogSnapshotStore {
    private final ProfessionalCategoryRepository categories;
    private final ProfessionalRepository professionals;
    private final WeeklyScheduleRepository schedules;
    private final CatalogSyncStateRepository syncStates;

    public JpaCatalogSnapshotStore(ProfessionalCategoryRepository categories,
            ProfessionalRepository professionals, WeeklyScheduleRepository schedules,
            CatalogSyncStateRepository syncStates) {
        this.categories = categories;
        this.professionals = professionals;
        this.schedules = schedules;
        this.syncStates = syncStates;
    }

    @Override
    @Transactional
    public boolean apply(CatalogSnapshot snapshot) {
        Objects.requireNonNull(snapshot, "snapshot is required");
        var state = syncStates.findForUpdate()
            .orElseThrow(() -> new IllegalStateException("Catalog synchronization state is missing"));
        if (state.getAppliedVersion() != null && snapshot.snapshotVersion() <= state.getAppliedVersion()) {
            return false;
        }

        // Borrar hijos primero e insertar padres primero respeta las claves foráneas.
        schedules.deleteAllInBatch();
        professionals.deleteAllInBatch();
        categories.deleteAllInBatch();
        categories.saveAllAndFlush(snapshot.professionalCategories().stream()
            .map(CatalogPersistenceMapper::toEntity).toList());
        professionals.saveAllAndFlush(snapshot.professionals().stream()
            .map(CatalogPersistenceMapper::toEntity).toList());
        schedules.saveAllAndFlush(snapshot.weeklySchedules().stream()
            .map(CatalogPersistenceMapper::toEntity).toList());
        state.markApplied(snapshot.snapshotVersion(), Instant.now());
        syncStates.flush();
        return true;
    }
}
