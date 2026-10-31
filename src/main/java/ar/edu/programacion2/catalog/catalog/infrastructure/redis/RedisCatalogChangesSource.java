package ar.edu.programacion2.catalog.catalog.infrastructure.redis;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogChanges;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogChangesReadException;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogChangesSource;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

public class RedisCatalogChangesSource implements CatalogChangesSource {
    private static final JsonMapper MAPPER = JsonMapper.builder()
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build();
    private final RedisClient client;

    public RedisCatalogChangesSource(RedisClient client) {
        this.client = client;
    }

    @Override
    public Optional<CatalogChanges> read(long version) {
        if (version < 0) {
            throw new IllegalArgumentException("Version must not be negative");
        }
        try (var connection = client.connect()) {
            var json = connection.sync().get("catedra:sync:changes:" + version);
            if (json == null) {
                return Optional.empty();
            }
            var root = MAPPER.readTree(json);
            if (root == null || !root.isObject() || integer(root.path("version")) != version) {
                throw new IllegalArgumentException("Unexpected changes version");
            }
            var changes = root.path("changes");
            return Optional.of(new CatalogChanges(version,
                ids(changes.path("professionalCategories")), ids(changes.path("professionals")),
                ids(changes.path("weeklySchedules"))));
        } catch (RedisException | JacksonException | IllegalArgumentException exception) {
            throw new CatalogChangesReadException();
        }
    }

    private static List<Long> ids(JsonNode node) {
        if (!node.isArray()) {
            throw new IllegalArgumentException("Changed IDs must be arrays");
        }
        var result = new ArrayList<Long>();
        for (var id : node) {
            result.add(integer(id));
        }
        return result;
    }

    private static long integer(JsonNode node) {
        if (!node.isIntegralNumber() || !node.canConvertToLong()) {
            throw new IllegalArgumentException("Expected an integer within long range");
        }
        return node.longValue();
    }
}
