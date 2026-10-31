package ar.edu.programacion2.catalog.catalog.application.model;

public record RemoteCatalogVersions(long currentVersion, long oldestAvailableVersion) {
    public RemoteCatalogVersions {
        if (currentVersion < 0 || oldestAvailableVersion < 0 || oldestAvailableVersion > currentVersion) {
            throw new IllegalArgumentException("Invalid catalog version range");
        }
    }
}
