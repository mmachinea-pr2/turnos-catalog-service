package ar.edu.programacion2.catalog.catalog.infrastructure.rest;

import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogSnapshotSource;
import ar.edu.programacion2.catalog.catalog.application.port.out.SnapshotFetchException;
import ar.edu.programacion2.catalog.catalog.domain.model.CatalogSnapshot;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

public class RestCatalogSnapshotSource implements CatalogSnapshotSource {
    private final RestClient client;

    public RestCatalogSnapshotSource(RestClient client) {
        this.client = Objects.requireNonNull(client);
    }

    @Override
    public CatalogSnapshot fetch() {
        try {
            var response = client.get().uri("/api/synchronization/snapshot")
                .accept(MediaType.APPLICATION_JSON).retrieve()
                .onStatus(status -> status.value() != 200, (request, result) -> {
                    throw new SnapshotFetchException("Snapshot request failed: HTTP " + result.getStatusCode().value());
                })
                .body(SnapshotResponse.class);
            return Objects.requireNonNull(response).toDomain();
        } catch (ResourceAccessException exception) {
            throw new SnapshotFetchException("Snapshot server unavailable or request timed out");
        } catch (RestClientException | IllegalArgumentException | NullPointerException exception) {
            // No propagar cuerpos remotos o causas de conversión que puedan contener secretos.
            throw new SnapshotFetchException("Invalid snapshot response");
        }
    }
}
