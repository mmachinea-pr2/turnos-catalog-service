package ar.edu.programacion2.catalog.catalog.infrastructure.startup;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalogSnapshot;
import ar.edu.programacion2.catalog.catalog.application.port.out.SnapshotFetchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Se activa solo mediante una opción explícita; el arranque normal no sincroniza. */
@Component
@ConditionalOnProperty(name = "catalog.synchronization.run-on-startup", havingValue = "true")
public class ManualCatalogSynchronization implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(ManualCatalogSynchronization.class);
    private final SynchronizeCatalogSnapshot synchronize;

    public ManualCatalogSynchronization(SynchronizeCatalogSnapshot synchronize) {
        this.synchronize = synchronize;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        final boolean applied;
        try {
            applied = synchronize.synchronize();
        } catch (SnapshotFetchException exception) {
            // El adaptador REST ya genera mensajes sin cuerpos remotos ni credenciales.
            throw new IllegalStateException("Catalog synchronization failed: " + exception.getMessage());
        } catch (RuntimeException exception) {
            // Los errores JPA pueden incluir datos remotos: no encadenarlos al error de arranque.
            throw new IllegalStateException("Catalog synchronization failed; no new snapshot was committed");
        }
        if (applied) {
            LOG.info("Catalog synchronization completed: snapshot applied");
        } else {
            LOG.info("Catalog synchronization completed: version already covered, no changes");
        }
    }
}
