package ar.edu.programacion2.catalog.catalog.infrastructure.messaging;

import ar.edu.programacion2.catalog.catalog.application.model.CatalogUpdatedEvent;
import java.time.Instant;
import java.time.DateTimeException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

public class CatalogUpdatedEventDecoder {
    private static final JsonMapper MAPPER = JsonMapper.builder()
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build();

    public CatalogUpdatedEvent decode(String key, String payload) {
        try {
            if (payload == null) {
                throw new IllegalArgumentException();
            }
            var root = MAPPER.readTree(payload);
            if (root == null || !root.isObject() || number(root, "schemaVersion") != 1
                    || !"CatalogUpdated".equals(text(root, "eventType"))) {
                throw new IllegalArgumentException();
            }
            long version = number(root, "newVersion");
            if (!Long.toString(version).equals(key)) {
                throw new IllegalArgumentException();
            }
            return new CatalogUpdatedEvent(text(root, "eventId"), version,
                Instant.parse(text(root, "occurredAt")), text(root, "source"));
        } catch (JacksonException | IllegalArgumentException | DateTimeException | NullPointerException exception) {
            throw new IllegalArgumentException("Invalid CatalogUpdated event");
        }
    }

    private static String text(JsonNode root, String name) {
        var value = root.path(name);
        if (!value.isString()) {
            throw new IllegalArgumentException();
        }
        return value.stringValue();
    }

    private static long number(JsonNode root, String name) {
        var value = root.path(name);
        if (!value.isIntegralNumber() || !value.canConvertToLong()) {
            throw new IllegalArgumentException();
        }
        return value.longValue();
    }
}
