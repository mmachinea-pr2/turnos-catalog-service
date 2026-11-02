package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogUpdatedEvent;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogEventConflictException;
import ar.edu.programacion2.catalog.catalog.application.port.out.ProcessedCatalogEventStore;
import java.nio.charset.StandardCharsets;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcProcessedCatalogEventStore implements ProcessedCatalogEventStore {
    private final JdbcTemplate jdbc;
    private final CatalogSyncStateRepository states;

    public JdbcProcessedCatalogEventStore(JdbcTemplate jdbc, CatalogSyncStateRepository states) {
        this.jdbc = jdbc;
        this.states = states;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isProcessed(CatalogUpdatedEvent event) {
        var versions = jdbc.queryForList("SELECT new_version FROM processed_catalog_event WHERE event_id=?",
            Long.class, event.eventId().getBytes(StandardCharsets.UTF_8));
        if (versions.isEmpty()) {
            return false;
        }
        if (versions.getFirst() != event.newVersion()) {
            throw new CatalogEventConflictException();
        }
        return true;
    }

    @Override
    @Transactional
    public void markProcessed(CatalogUpdatedEvent event) {
        // Comparte el bloqueo con snapshots e incrementos; nunca abarca llamadas externas.
        var state = states.findForUpdate().orElseThrow(() -> new IllegalStateException("Catalog state is missing"));
        if (state.getAppliedVersion() == null || state.getAppliedVersion() < event.newVersion()) {
            throw new IllegalStateException("Catalog version does not cover the event");
        }
        if (!isProcessed(event)) {
            jdbc.update("INSERT INTO processed_catalog_event(event_id,new_version,processed_at) VALUES (?,?,UTC_TIMESTAMP(6))",
                event.eventId().getBytes(StandardCharsets.UTF_8), event.newVersion());
        }
    }
}
