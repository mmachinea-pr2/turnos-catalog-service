package ar.edu.programacion2.catalog.catalog.infrastructure.scheduling;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Una comprobación por invocación; la planificación se configura por separado. */
public final class PeriodicCatalogSynchronization implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(PeriodicCatalogSynchronization.class);
    private final SynchronizeCatalog synchronize;
    private final AtomicBoolean running = new AtomicBoolean();

    public PeriodicCatalogSynchronization(SynchronizeCatalog synchronize) {
        this.synchronize = Objects.requireNonNull(synchronize);
    }

    @Override
    public void run() {
        if (!running.compareAndSet(false, true)) {
            return;
        }
        try {
            var result = Objects.requireNonNull(synchronize.synchronize());
            switch (result) {
                case RETRY_REQUIRED -> LOG.warn("Periodic catalog synchronization pending; wait for the next check");
                case LOCAL_VERSION_AHEAD -> LOG.warn("Periodic catalog synchronization: local version ahead; review remote state");
                default -> LOG.info("Periodic catalog synchronization: {}", result);
            }
        } catch (RuntimeException exception) {
            // No registrar causas que puedan contener credenciales o respuestas remotas.
            LOG.warn("Periodic catalog synchronization failed; wait for the next check");
        } finally {
            running.set(false);
        }
    }
}
