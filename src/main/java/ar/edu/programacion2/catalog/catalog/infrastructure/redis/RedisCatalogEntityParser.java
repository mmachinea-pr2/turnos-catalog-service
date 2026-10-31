package ar.edu.programacion2.catalog.catalog.infrastructure.redis;

import ar.edu.programacion2.catalog.catalog.domain.model.ProfessionalCategory;
import ar.edu.programacion2.catalog.catalog.domain.model.Professional;
import ar.edu.programacion2.catalog.catalog.domain.model.WeeklySchedule;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

final class RedisCatalogEntityParser {
    private static final JsonMapper MAPPER = JsonMapper.builder()
        .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS).build();

    private RedisCatalogEntityParser() { }

    static ProfessionalCategory category(String json, long id) {
        var node = entity(json, id);
        var description = node.path("description");
        if (!description.isMissingNode() && !description.isNull() && !description.isString()) {
            throw new IllegalArgumentException("Invalid description");
        }
        return new ProfessionalCategory(id, text(node, "name"),
            description.isString() ? description.stringValue() : null, enabled(node),
            instant(node, "createdAt"), instant(node, "updatedAt"));
    }

    static Professional professional(String json, long id) {
        var node = entity(json, id);
        return new Professional(id, positiveInteger(node, "categoryId"), text(node, "firstName"),
            text(node, "lastName"), enabled(node), instant(node, "createdAt"), instant(node, "updatedAt"));
    }

    static WeeklySchedule schedule(String json, long id) {
        var node = entity(json, id);
        long duration = positiveInteger(node, "slotDurationMinutes");
        if (duration > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid slot duration");
        }
        return new WeeklySchedule(id, positiveInteger(node, "professionalId"),
            DayOfWeek.valueOf(text(node, "dayOfWeek")), LocalTime.parse(text(node, "startTime")),
            LocalTime.parse(text(node, "endTime")), (int) duration, enabled(node),
            instant(node, "createdAt"), instant(node, "updatedAt"));
    }

    private static JsonNode entity(String json, long id) {
        var node = MAPPER.readTree(json);
        if (node == null || !node.isObject() || positiveInteger(node, "id") != id) {
            throw new IllegalArgumentException("Unexpected entity ID");
        }
        return node;
    }

    private static long positiveInteger(JsonNode node, String name) {
        var value = node.path(name);
        if (!value.isIntegralNumber() || !value.canConvertToLong() || value.longValue() <= 0) {
            throw new IllegalArgumentException("Expected positive integer");
        }
        return value.longValue();
    }

    private static String text(JsonNode node, String name) {
        var value = node.path(name);
        if (!value.isString()) {
            throw new IllegalArgumentException("Expected text");
        }
        return value.stringValue();
    }

    private static boolean enabled(JsonNode node) {
        var value = node.path("enabled");
        if (!value.isBoolean()) {
            throw new IllegalArgumentException("Expected enabled flag");
        }
        return value.booleanValue();
    }

    private static Instant instant(JsonNode node, String name) {
        return Instant.parse(text(node, name));
    }
}
