package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationAttempt;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationAttemptStore;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class JdbcCatalogSynchronizationAttemptStore implements CatalogSynchronizationAttemptStore {
    private final JdbcTemplate jdbc;

    public JdbcCatalogSynchronizationAttemptStore(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void start(UUID attemptId, Instant startedAt) {
        int updated = jdbc.update("""
            UPDATE catalog_sync_attempt SET attempt_id=?, started_at=?, completed_at=NULL,
                result='RUNNING', error_code=NULL WHERE id=1
            """, attemptId.toString(), Timestamp.from(startedAt));
        if (updated != 1) throw new IllegalStateException("Missing synchronization attempt state");
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void complete(UUID attemptId, Instant completedAt, String result, String errorCode) {
        // Un intento anterior que termina tarde no reemplaza al más recientemente iniciado.
        jdbc.update("""
            UPDATE catalog_sync_attempt SET completed_at=?, result=?, error_code=?
            WHERE id=1 AND attempt_id=?
            """, Timestamp.from(completedAt), result, errorCode, attemptId.toString());
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogSynchronizationAttempt read() {
        return jdbc.queryForObject("SELECT * FROM catalog_sync_attempt WHERE id=1", (row, index) -> {
            var started = row.getTimestamp("started_at");
            var completed = row.getTimestamp("completed_at");
            return new CatalogSynchronizationAttempt(started == null ? null : started.toInstant(),
                completed == null ? null : completed.toInstant(), row.getString("result"), row.getString("error_code"));
        });
    }
}
