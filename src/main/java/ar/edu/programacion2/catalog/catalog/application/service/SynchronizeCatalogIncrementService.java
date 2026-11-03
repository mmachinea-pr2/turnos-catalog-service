package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogChangedEntities;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogChanges;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogIncrementResult;
import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogIncrement;
import ar.edu.programacion2.catalog.catalog.application.port.out.*;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

/** Performs external reads before entering the persistence transaction. */
public class SynchronizeCatalogIncrementService implements SynchronizeCatalogIncrement {
    private final CatalogSynchronizationStatusReader statusReader;
    private final CatalogVersionSource versions;
    private final CatalogChangesSource changesSource;
    private final CatalogEntitiesSource entitiesSource;
    private final CatalogIncrementStore store;

    public SynchronizeCatalogIncrementService(CatalogSynchronizationStatusReader statusReader,
            CatalogVersionSource versions, CatalogChangesSource changesSource,
            CatalogEntitiesSource entitiesSource, CatalogIncrementStore store) {
        this.statusReader = statusReader;
        this.versions = versions;
        this.changesSource = changesSource;
        this.entitiesSource = entitiesSource;
        this.store = store;
    }

    @Override
    public CatalogIncrementResult synchronize() {
        var local = statusReader.read().appliedVersion();
        if (local != null && local < 0) {
            throw new IllegalStateException("Local catalog version must not be negative");
        }
        var before = versions.read();
        if (local == null) {
            return CatalogIncrementResult.SNAPSHOT_REQUIRED;
        }
        if (local > before.currentVersion()) {
            return CatalogIncrementResult.LOCAL_VERSION_AHEAD;
        }
        if (local == before.currentVersion()) {
            return CatalogIncrementResult.UP_TO_DATE;
        }
        // Los Hashes actuales no permiten reconstruir estados intermedios históricos.
        if (local < before.oldestAvailableVersion() || before.currentVersion() - local != 1) {
            return CatalogIncrementResult.SNAPSHOT_REQUIRED;
        }
        var optionalChanges = changesSource.read(before.currentVersion());
        if (optionalChanges.isEmpty()) {
            return CatalogIncrementResult.SNAPSHOT_REQUIRED;
        }
        var changes = optionalChanges.orElseThrow();
        if (changes.version() != before.currentVersion()) {
            throw new CatalogChangesReadException();
        }
        var entities = entitiesSource.read(changes);
        validateBatch(changes, entities);
        var after = versions.read();
        if (!before.equals(after)) {
            return CatalogIncrementResult.RETRY_REQUIRED;
        }
        return store.apply(entities) ? CatalogIncrementResult.APPLIED : CatalogIncrementResult.ALREADY_APPLIED;
    }

    private static void validateBatch(CatalogChanges changes, CatalogChangedEntities entities) {
        if (entities.version() != changes.version()
                || !sameIds(changes.professionalCategories(), entities.professionalCategories(), entity -> entity.id())
                || !sameIds(changes.professionals(), entities.professionals(), entity -> entity.id())
                || !sameIds(changes.weeklySchedules(), entities.weeklySchedules(), entity -> entity.id())) {
            throw new CatalogEntitiesReadException();
        }
    }

    private static <T> boolean sameIds(List<Long> expected, List<T> actual, Function<T, Long> identifier) {
        return expected.size() == actual.size()
            && new HashSet<>(expected).equals(new HashSet<>(actual.stream().map(identifier).toList()));
    }
}
