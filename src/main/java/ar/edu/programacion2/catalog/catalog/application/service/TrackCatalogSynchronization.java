package ar.edu.programacion2.catalog.catalog.application.service;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSynchronizationAttemptStore;
import java.time.Clock;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/** Registra un intento exterior; un snapshot de recuperación pertenece al mismo intento. */
public final class TrackCatalogSynchronization {
    private final CatalogSynchronizationAttemptStore store;
    private final Clock clock;
    private final ThreadLocal<Boolean> active = new ThreadLocal<>();

    public TrackCatalogSynchronization(CatalogSynchronizationAttemptStore store, Clock clock) {
        this.store = store;
        this.clock = clock;
    }

    public <T> T execute(Supplier<T> operation, Function<T, String> describe) {
        if (Boolean.TRUE.equals(active.get())) return operation.get();
        var id = UUID.randomUUID();
        store.start(id, clock.instant());
        active.set(true);
        try {
            final T value;
            final String result;
            try {
                value = operation.get();
                result = describe.apply(value);
            } catch (RuntimeException failure) {
                try {
                    store.complete(id, clock.instant(), "FAILED", "SYNCHRONIZATION_FAILED");
                } catch (RuntimeException recordingFailure) {
                    // Conservar la causa original; no exponer mensajes de infraestructura.
                    failure.addSuppressed(recordingFailure);
                }
                throw failure;
            }
            store.complete(id, clock.instant(), result, null);
            return value;
        } finally {
            active.remove();
        }
    }
}
