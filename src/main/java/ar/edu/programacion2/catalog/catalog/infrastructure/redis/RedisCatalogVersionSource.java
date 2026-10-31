package ar.edu.programacion2.catalog.catalog.infrastructure.redis;

import ar.edu.programacion2.catalog.catalog.application.model.RemoteCatalogVersions;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionReadException;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogVersionSource;
import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;

public class RedisCatalogVersionSource implements CatalogVersionSource {
    private final RedisClient client;

    public RedisCatalogVersionSource(RedisClient client) {
        this.client = client;
    }

    @Override
    public RemoteCatalogVersions read() {
        try (var connection = client.connect()) {
            var values = connection.sync().mget(
                "catedra:sync:current-version", "catedra:sync:oldest-available-version");
            return new RemoteCatalogVersions(parse(values.get(0)), parse(values.get(1)));
        } catch (RedisException | IllegalArgumentException exception) {
            // No incluir mensajes del servidor, direcciones o credenciales en el error público.
            throw new CatalogVersionReadException();
        }
    }

    private long parse(KeyValue<String, String> value) {
        if (!value.hasValue() || !value.getValue().matches("[0-9]+")) {
            throw new IllegalArgumentException("Missing or invalid catalog version");
        }
        return Long.parseLong(value.getValue());
    }
}
