package ar.edu.programacion2.catalog.catalog.infrastructure.startup;

import ar.edu.programacion2.catalog.catalog.application.port.in.SynchronizeCatalog;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogSynchronizationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "catalog.synchronization.full-run-on-startup", havingValue = "true")
public class ManualFullCatalogSynchronization implements ApplicationRunner {
    private static final Logger LOG = LoggerFactory.getLogger(ManualFullCatalogSynchronization.class);
    private final SynchronizeCatalog synchronize;

    public ManualFullCatalogSynchronization(SynchronizeCatalog synchronize,
            @Value("${catalog.synchronization.run-on-startup:false}") boolean snapshotMode,
            @Value("${spring.main.web-application-type:servlet}") String webType) {
        if (snapshotMode) {
            throw new IllegalArgumentException("Choose only one manual catalog synchronization mode");
        }
        if (!"none".equalsIgnoreCase(webType)) {
            throw new IllegalArgumentException("Full manual synchronization requires non-web mode");
        }
        this.synchronize = synchronize;
    }

    @Override
    public void run(ApplicationArguments arguments) {
        final CatalogSynchronizationResult result;
        try {
            result = java.util.Objects.requireNonNull(synchronize.synchronize());
        } catch (RuntimeException exception) {
            // No propagar cuerpos remotos, SQL ni credenciales de las causas.
            throw new IllegalStateException("Full catalog synchronization failed; review connectivity and configuration");
        }
        switch (result) {
            case RETRY_REQUIRED -> throw new IllegalStateException(
                "Full catalog synchronization incomplete: remote versions changed; run again later");
            case LOCAL_VERSION_AHEAD -> throw new IllegalStateException(
                "Full catalog synchronization stopped: local version is ahead; review remote state");
            case INCREMENT_APPLIED -> LOG.info("Full catalog synchronization: increment applied");
            case SNAPSHOT_APPLIED -> LOG.info("Full catalog synchronization: snapshot applied");
            case UP_TO_DATE -> LOG.info("Full catalog synchronization: local version matches observed remote version");
            case ALREADY_APPLIED -> LOG.info("Full catalog synchronization: increment already covered locally");
            case SNAPSHOT_ALREADY_COVERED -> LOG.info("Full catalog synchronization: snapshot already covered locally");
        }
    }
}
