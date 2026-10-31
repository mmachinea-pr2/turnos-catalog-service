package ar.edu.programacion2.catalog.catalog.infrastructure.redis;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogChanges;
import ar.edu.programacion2.catalog.catalog.application.model.CatalogChangedEntities;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogEntitiesSource;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogEntitiesReadException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisException;
import io.lettuce.core.api.sync.RedisCommands;
import java.time.DateTimeException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import tools.jackson.core.JacksonException;

public class RedisCatalogEntitiesSource implements CatalogEntitiesSource {
    private final RedisClient client;

    public RedisCatalogEntitiesSource(RedisClient client) {
        this.client = client;
    }

    @Override
    public CatalogChangedEntities read(CatalogChanges changes) {
        Objects.requireNonNull(changes);
        if (changes.professionalCategories().isEmpty() && changes.professionals().isEmpty()
                && changes.weeklySchedules().isEmpty()) {
            return new CatalogChangedEntities(changes.version(), List.of(), List.of(), List.of());
        }
        try (var connection = client.connect()) {
            var commands = connection.sync();
            return new CatalogChangedEntities(changes.version(),
                readEntities(commands, "professional-categories", changes.professionalCategories(), RedisCatalogEntityParser::category),
                readEntities(commands, "professionals", changes.professionals(), RedisCatalogEntityParser::professional),
                readEntities(commands, "weekly-schedules", changes.weeklySchedules(), RedisCatalogEntityParser::schedule));
        } catch (RedisException | JacksonException | IllegalArgumentException | DateTimeException exception) {
            throw new CatalogEntitiesReadException();
        }
    }

    private static <T> List<T> readEntities(RedisCommands<String, String> commands, String hash,
            List<Long> ids, BiFunction<String, Long, T> parser) {
        if (ids.isEmpty()) {
            return List.of();
        }
        var fields = ids.stream().map(String::valueOf).toArray(String[]::new);
        var values = commands.hmget("catedra:sync:" + hash, fields);
        var result = new ArrayList<T>();
        for (int index = 0; index < ids.size(); index++) {
            var value = values.get(index);
            if (!value.hasValue()) {
                throw new IllegalArgumentException("Requested entity is missing");
            }
            result.add(parser.apply(value.getValue(), ids.get(index)));
        }
        return result;
    }
}
