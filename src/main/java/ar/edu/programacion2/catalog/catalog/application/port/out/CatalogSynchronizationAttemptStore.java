package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationAttempt;
import java.time.Instant;
import java.util.UUID;

public interface CatalogSynchronizationAttemptStore {
    void start(UUID attemptId, Instant startedAt);
    void complete(UUID attemptId, Instant completedAt, String result, String errorCode);
    CatalogSynchronizationAttempt read();
}
