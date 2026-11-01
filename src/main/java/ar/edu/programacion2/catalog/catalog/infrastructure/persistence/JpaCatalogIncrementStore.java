package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogChangedEntities;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogIncrementStore;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogIncrementRejectedException;
import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JpaCatalogIncrementStore implements CatalogIncrementStore {
    private final ProfessionalCategoryRepository categories;
    private final ProfessionalRepository professionals;
    private final WeeklyScheduleRepository schedules;
    private final CatalogSyncStateRepository syncStates;

    public JpaCatalogIncrementStore(ProfessionalCategoryRepository categories,
            ProfessionalRepository professionals, WeeklyScheduleRepository schedules,
            CatalogSyncStateRepository syncStates) {
        this.categories = categories;
        this.professionals = professionals;
        this.schedules = schedules;
        this.syncStates = syncStates;
    }

    @Override
    @Transactional
    public boolean apply(CatalogChangedEntities changes) {
        Objects.requireNonNull(changes, "changes are required");
        var state = syncStates.findForUpdate()
            .orElseThrow(() -> new CatalogIncrementRejectedException("Catalog synchronization state is missing"));
        var applied = state.getAppliedVersion();
        if (applied == null) {
            throw new CatalogIncrementRejectedException("An initial snapshot is required before applying increments");
        }
        if (changes.version() <= applied) {
            return false;
        }
        if (changes.version() - applied != 1) {
            throw new CatalogIncrementRejectedException("Catalog increments must be applied consecutively");
        }
        // Upsert de padres antes que hijos; las entidades ausentes del lote se conservan.
        categories.saveAllAndFlush(changes.professionalCategories().stream()
            .map(CatalogPersistenceMapper::toEntity).toList());
        professionals.saveAllAndFlush(changes.professionals().stream()
            .map(CatalogPersistenceMapper::toEntity).toList());
        schedules.saveAllAndFlush(changes.weeklySchedules().stream()
            .map(CatalogPersistenceMapper::toEntity).toList());
        state.markApplied(changes.version(), Instant.now());
        syncStates.flush();
        return true;
    }
}
