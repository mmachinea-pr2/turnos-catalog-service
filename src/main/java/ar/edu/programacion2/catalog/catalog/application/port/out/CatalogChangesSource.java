package ar.edu.programacion2.catalog.catalog.application.port.out;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogChanges;
import java.util.Optional;

public interface CatalogChangesSource {
    /** Empty means the requested version is no longer available, not that it has no changes. */
    Optional<CatalogChanges> read(long version);
}
